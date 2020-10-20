package io.coti.basenode.services;

import com.google.gson.Gson;
import io.coti.basenode.crypto.NodeCryptoHelper;
import io.coti.basenode.data.DbRestoreSource;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.NetworkNodeData;
import io.coti.basenode.database.interfaces.IDatabaseConnector;
import io.coti.basenode.exceptions.CotiRunTimeException;
import io.coti.basenode.exceptions.DataBaseRecoveryException;
import io.coti.basenode.exceptions.DataBaseRestoreException;
import io.coti.basenode.http.GetBackupBucketResponse;
import io.coti.basenode.http.Response;
import io.coti.basenode.http.SerializableResponse;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.services.interfaces.IAwsService;
import io.coti.basenode.services.interfaces.IDBRecoveryService;
import io.coti.basenode.services.interfaces.INetworkService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.NOT_BACKUP_NODE;
import static io.coti.basenode.http.BaseNodeHttpStringConstants.STATUS_ERROR;

@Slf4j
@Service
public class BaseNodeDBRecoveryService implements IDBRecoveryService {

    private static final int INDEX_OF_BACKUP_TIMESTAMP_IN_PATH = 3;
    private static final int INDEX_OF_BACKUP_TIMESTAMP_IN_FOLDER_NAME = 1;
    private static final int ALLOWED_NUMBER_OF_BACKUPS = 2;
    private static final String BACK_UP_FOLDER_PREFIX = "/backup-";
    @Value("${db.backup}")
    private boolean backup;
    @Value("${db.backup.bucket}")
    private String backupBucket;
    @Value("${db.restore.backup.local}")
    private boolean backupToLocalWhenRestoring;
    @Value("${db.backup.time}")
    private String backupTime;
    @Value("${database.folder.name}")
    private String databaseFolderName;
    @Value("${application.name}")
    private String applicationName;
    @Value("${network}")
    private String network;
    @Value("${db.restore}")
    private boolean restore;
    @Value("${db.restore.hash}")
    private Hash restoreNodeHash;
    @Value("${db.restore.source}")
    private DbRestoreSource restoreSource;
    @Autowired
    private IDatabaseConnector dBConnector;
    @Autowired
    private IAwsService awsService;
    @Autowired
    private INetworkService networkService;
    @Autowired
    private RestTemplate restTemplate;
    private String localBackupFolderPath;
    private String remoteBackupFolderPath;
    private String backupS3Path;
    private String restoreS3Path;

    @Override
    public void init() {
        try {
            String dbPath = dBConnector.getDBPath();
            localBackupFolderPath = dbPath + "/backups/local";
            remoteBackupFolderPath = dbPath + "/backups/remote";
            createBackupFolder(localBackupFolderPath);
            createBackupFolder(remoteBackupFolderPath);
            validateInjectedProperties();
            initBackupNodeHashS3Path();
            if (restore) {
                restoreDB();
            }
        } catch (DataBaseRecoveryException e) {
            throw new DataBaseRecoveryException("Recovery service init error.\n" + e.getMessage(), e);
        } catch (Exception e) {
            throw new DataBaseRecoveryException("Recovery service init error.", e);
        }
    }

    private void validateInjectedProperties() {
        if (backup) {
            if (!awsService.isBuildS3ClientWithCredentials()) {
                throw new DataBaseRecoveryException("Aws s3 client should be with credentials when backup flag is set to true");
            }
            if (backupBucket.isEmpty()) {
                throw new DataBaseRecoveryException("Aws backup bucket can not be empty when backup flag is set to true");
            }
        }
        if (restore) {
            if (restoreNodeHash.toString().isEmpty()) {
                throw new DataBaseRecoveryException("Restore node hash can not be empty when restore flag is set to true");
            }
            if (restoreSource.equals(DbRestoreSource.Remote) && NodeCryptoHelper.getNodeHash().equals(restoreNodeHash) && backupBucket.isEmpty()) {
                throw new DataBaseRecoveryException("Aws backup bucket can not be empty when restore flag is set to true and remote restore is from the node's own backup");
            }
        }

    }

    @Scheduled(cron = "${db.backup.time}", zone = "UTC")
    private void backupDB() {
        if (backup) {
            try {
                log.info("Starting DB backup flow");
                deleteBackup(remoteBackupFolderPath);
                dBConnector.generateDataBaseBackup(remoteBackupFolderPath);
                List<String> backupFiles = awsService.listS3Paths(backupBucket, backupS3Path);
                if (backupFiles.isEmpty()) {
                    awsService.createS3Folder(backupBucket, backupS3Path);
                }
                File backupFolderToUpload = new File(remoteBackupFolderPath);
                log.info("Uploading remote backup to S3 bucket");
                awsService.uploadFolderAndContentsToS3(backupBucket, backupS3Path + BACK_UP_FOLDER_PREFIX + Instant.now().toEpochMilli(), backupFolderToUpload);
                if (!backupFiles.isEmpty()) {
                    Set<Long> s3BackupTimeStampSet = getS3BackupTimeStampSet(backupFiles);
                    if (s3BackupTimeStampSet.size() >= ALLOWED_NUMBER_OF_BACKUPS) {
                        List<Long> s3BackupTimeStamps = new ArrayList<>(s3BackupTimeStampSet);
                        Collections.sort(s3BackupTimeStamps);
                        String[] backupFoldersToRemove = s3BackupTimeStamps.stream().limit((long) s3BackupTimeStampSet.size() - ALLOWED_NUMBER_OF_BACKUPS + 1).map(s3BackupTimeStamp -> backupS3Path + BACK_UP_FOLDER_PREFIX + s3BackupTimeStamp.toString()).toArray(String[]::new);
                        backupFiles = backupFiles.stream().filter(backupFile -> StringUtils.startsWithAny(backupFile, backupFoldersToRemove)).collect(Collectors.toList());
                        awsService.deleteFolderAndContentsFromS3(backupFiles, backupBucket);
                    }
                }
                log.info("Finished DB backup flow");
            } catch (CotiRunTimeException e) {
                log.error("Backup DB error.");
                e.logMessage();
            } catch (Exception e) {
                log.error("Backup DB error.\n{}: {}", e.getClass().getName(), e.getMessage());
            } finally {
                deleteBackup(remoteBackupFolderPath);
            }
        }
    }

    private void restoreDB() {
        try {
            log.info("Starting DB restore flow");
            if (restoreSource.equals(DbRestoreSource.Local)) {
                log.info("Restoring from local backup");
                dBConnector.restoreDataBase(localBackupFolderPath);
            } else {
                if (backupToLocalWhenRestoring) {
                    deleteBackup(localBackupFolderPath);
                    dBConnector.generateDataBaseBackup(localBackupFolderPath);
                }
                restoreDBFromRemote();
            }
            log.info("Finished DB restore flow");
        } catch (DataBaseRecoveryException e) {
            throw new DataBaseRecoveryException("Restore database error.\n" + e.getMessage(), e);
        } catch (Exception e) {
            throw new DataBaseRecoveryException("Restore database error.", e);
        }

    }

    private void restoreDBFromRemote() {
        try {
            deleteBackup(remoteBackupFolderPath);
            final String restoreBucket = getBackupBucketFromRestoreNode();
            List<String> s3BackupFolderAndContents = awsService.listS3Paths(restoreBucket, restoreS3Path);
            if (s3BackupFolderAndContents.isEmpty()) {
                throw new DataBaseRestoreException(String.format("Couldn't complete restore. No backups found at %s/%s", restoreBucket, restoreS3Path));

            }
            String latestS3Backup = getLatestS3Backup(s3BackupFolderAndContents, restoreS3Path);
            log.info("Downloading remote backup from S3 bucket");
            awsService.downloadFolderAndContents(restoreBucket, latestS3Backup, remoteBackupFolderPath);
            dBConnector.restoreDataBase(remoteBackupFolderPath);
        } catch (Exception e) {
            dBConnector.restoreDataBase(localBackupFolderPath);
            throw e;
        } finally {
            deleteBackup(remoteBackupFolderPath);
        }
    }

    private String getBackupBucketFromRestoreNode() {

        if (NodeCryptoHelper.getNodeHash().equals(restoreNodeHash)) {
            return backupBucket;
        }
        NetworkNodeData networkNodeData = networkService.getNetworkNodeData();
        Map<Hash, NetworkNodeData> networkNodeDataMap = networkService.getMapFromFactory(networkNodeData.getNodeType());
        if (networkNodeDataMap.isEmpty() || networkNodeDataMap.get(restoreNodeHash) == null) {
            throw new DataBaseRestoreException("Restore node is either not existing or not active in Coti Network.");
        }
        NetworkNodeData restoreNodeData = networkNodeDataMap.get(restoreNodeHash);
        String restoreNodeHttpAddress = restoreNodeData.getHttpFullAddress();

        try {
            GetBackupBucketResponse getBackupBucketResponse = restTemplate.getForObject(restoreNodeHttpAddress + "/backup/bucket", GetBackupBucketResponse.class);
            if (getBackupBucketResponse == null || getBackupBucketResponse.getBackupBucket() == null) {
                throw new DataBaseRestoreException("Null backup bucket received from restore node.");
            }
            return getBackupBucketResponse.getBackupBucket();

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new DataBaseRecoveryException(String.format("Get backup bucket from restore node error. Recovery node response: %s", new Gson().fromJson(e.getResponseBodyAsString(), Response.class).getMessage()), e);
        } catch (Exception e) {
            throw new DataBaseRecoveryException("Get backup bucket from restore node error.", e);
        }

    }

    @Override
    public ResponseEntity<IResponse> getBackupBucket() {
        if (!backup) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new SerializableResponse(NOT_BACKUP_NODE, STATUS_ERROR));
        }

        return ResponseEntity.ok(new GetBackupBucketResponse(backupBucket));
    }

    private void deleteBackup(String backupFolderPath) {
        try {
            FileUtils.cleanDirectory(new File(backupFolderPath));
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void initBackupNodeHashS3Path() {
        String folderDelimiter = "/";
        StringBuilder sb = new StringBuilder(network);
        sb.append(folderDelimiter).append(applicationName).append(folderDelimiter);
        if (backup) {
            backupS3Path = sb.toString() + NodeCryptoHelper.getNodeHash();
        }
        if (restore) {
            restoreS3Path = sb.toString() + restoreNodeHash.toString();
        }
    }

    private void createBackupFolder(String folderPath) {
        File directory = new File(folderPath);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    private String getLatestS3Backup(List<String> remoteBackups, String backupNodeHashS3Path) {
        Set<Long> s3Backups = getS3BackupTimeStampSet(remoteBackups);
        Long backupTimeStamp = Collections.max(s3Backups);
        return backupNodeHashS3Path + BACK_UP_FOLDER_PREFIX + backupTimeStamp.toString();
    }

    private Set<Long> getS3BackupTimeStampSet(List<String> remoteBackups) {
        String folderDelimiter = "/";
        String folderNameDelimiter = "-";
        Set<Long> s3Backups = new HashSet<>();
        remoteBackups.forEach(remoteBackup -> {
            String[] backupPathArray = remoteBackup.split(folderDelimiter);
            if (backupPathArray.length > INDEX_OF_BACKUP_TIMESTAMP_IN_PATH) {
                String[] folderNameArray = backupPathArray[INDEX_OF_BACKUP_TIMESTAMP_IN_PATH].split(folderNameDelimiter);
                if (folderNameArray.length > INDEX_OF_BACKUP_TIMESTAMP_IN_FOLDER_NAME)
                    s3Backups.add(Long.parseLong(folderNameArray[INDEX_OF_BACKUP_TIMESTAMP_IN_FOLDER_NAME]));
            }
        });
        return s3Backups;
    }

}
