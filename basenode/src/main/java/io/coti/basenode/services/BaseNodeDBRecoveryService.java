package io.coti.basenode.services;

import com.google.gson.Gson;
import io.coti.basenode.data.DbRestoreSource;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.NetworkNodeData;
import io.coti.basenode.exceptions.CotiRunTimeException;
import io.coti.basenode.exceptions.DataBaseBackupException;
import io.coti.basenode.exceptions.DataBaseRecoveryException;
import io.coti.basenode.exceptions.DataBaseRestoreException;
import io.coti.basenode.http.GetBackupBucketResponse;
import io.coti.basenode.http.Response;
import io.coti.basenode.http.SerializableResponse;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.services.interfaces.IDBRecoveryService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.rocksdb.BackupInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.*;
import static io.coti.basenode.services.BaseNodeServiceManager.*;


@Slf4j
@Service
public class BaseNodeDBRecoveryService implements IDBRecoveryService {

    private static final int INDEX_OF_BACKUP_TIMESTAMP_IN_PATH = 3;
    private static final int INDEX_OF_BACKUP_TIMESTAMP_IN_FOLDER_NAME = 1;
    private static final int ALLOWED_NUMBER_OF_BACKUPS = 2;
    private static final String BACK_UP_FOLDER_PREFIX = "/backup-";

    @Getter
    private final AtomicBoolean backupInProgress = new AtomicBoolean(false);
    @Getter
    @Value("${db.backup}")
    private boolean backup;
    @Value("${db.backup.manual:false}")
    private boolean manualBackup;
    @Value("${db.backup.bucket}")
    private String backupBucket;
    @Value("${db.restore.backup.local}")
    private boolean backupToLocalWhenRestoring;
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

    private String localBackupFolderPath;
    private String remoteBackupFolderPath;
    private String backupS3Path;
    private String restoreS3Path;
    @Getter
    private String s3FolderName;

    @Getter
    private BackupInfo lastBackupInfo;
    @Getter
    private long lastBackupStartedTime;
    @Getter
    private long lastEntireDuration;
    @Getter
    private long lastBackupDuration;
    @Getter
    private long lastUploadDuration;
    @Getter
    private long lastRemovalDuration;
    @Getter
    private long lastBackupSuccess;


    @Override
    public void init() {
        try {
            String dbPath = databaseConnector.getDBPath();
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
        if (backup || manualBackup) {
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
            if (restoreSource.equals(DbRestoreSource.Remote) && nodeIdentityService.getNodeHash().equals(restoreNodeHash) && backupBucket.isEmpty()) {
                throw new DataBaseRecoveryException("Aws backup bucket can not be empty when restore flag is set to true and remote restore is from the node's own backup");
            }
        }

    }

    private void removeOlderBackupsFromS3(List<String> backupFiles) {
        if (!backupFiles.isEmpty()) {
            Set<Long> s3BackupTimeStampSet = getS3BackupTimeStampSet(backupFiles);
            if (s3BackupTimeStampSet.size() >= ALLOWED_NUMBER_OF_BACKUPS) {
                List<Long> s3BackupTimeStamps = new ArrayList<>(s3BackupTimeStampSet);
                Collections.sort(s3BackupTimeStamps);
                String[] backupFoldersToRemove = s3BackupTimeStamps.stream().limit((long) s3BackupTimeStampSet.size() - ALLOWED_NUMBER_OF_BACKUPS + 1).map(s3BackupTimeStamp -> backupS3Path + BACK_UP_FOLDER_PREFIX + s3BackupTimeStamp.toString()).toArray(String[]::new);
                List<String> backupFilesToRemove = backupFiles.stream().filter(backupFile -> StringUtils.startsWithAny(backupFile, backupFoldersToRemove)).collect(Collectors.toList());
                log.info("Deleting {} older backup folder(s) with total {} file(s).", backupFoldersToRemove.length, backupFilesToRemove.size());
                log.info("Folders: {}", Arrays.toString(backupFoldersToRemove));
                awsService.deleteFolderAndContentsFromS3(backupFilesToRemove, backupBucket);
                log.info("Finished to delete older backup folders");
            }
        }
    }

    private void uploadRecentBackupToS3(List<String> backupFiles) {
        if (backupFiles.isEmpty()) {
            awsService.createS3Folder(backupBucket, backupS3Path);
        }
        File backupFolderToUpload = new File(remoteBackupFolderPath);
        s3FolderName = BACK_UP_FOLDER_PREFIX + Instant.now().toEpochMilli();
        log.info("Uploading remote backup to S3 bucket {} and folderName {}", backupBucket, s3FolderName);
        awsService.uploadFolderAndContentsToS3(backupBucket, backupS3Path + s3FolderName, backupFolderToUpload);
    }

    private List<String> getBackupFiles() {
        return awsService.listS3Paths(backupBucket, backupS3Path);
    }

    @Scheduled(cron = "${db.backup.time}", zone = "UTC")
    private void backupDBCron() {
        if (backup) {
            try {
                backupDB();
            } catch (Exception e) {
                log.error("Error at backup DB cron");
            }
        }
    }

    public ResponseEntity<IResponse> manualBackupDB() {
        try {
            if (!manualBackup) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(DB_MANUAL_BACKUP_NOT_ALLOWED));
            }
            log.info("Manual DB backup initialized");
            backupDB();
            return ResponseEntity.ok(new Response(DB_MANUAL_BACKUP_SUCCESS));
        } catch (Exception e) {
            log.error("Error at manual backup DB");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response(e.getMessage()));
        }
    }

    private void backupDB() {
        if (backupInProgress.compareAndSet(false, true)) {
            try {
                lastBackupStartedTime = java.time.Instant.now().getEpochSecond();
                log.info("Starting DB backup flow");
                deleteBackup(remoteBackupFolderPath);
                lastBackupInfo = databaseConnector.generateDataBaseBackup(remoteBackupFolderPath);
                lastBackupDuration = java.time.Instant.now().getEpochSecond() - lastBackupInfo.timestamp();
                List<String> uploadedBackupFiles = getBackupFiles();
                long uploadBackupStartedTime = java.time.Instant.now().getEpochSecond();
                uploadRecentBackupToS3(uploadedBackupFiles);
                lastUploadDuration = java.time.Instant.now().getEpochSecond() - uploadBackupStartedTime;
                long removalBackupStartedTime = java.time.Instant.now().getEpochSecond();
                removeOlderBackupsFromS3(uploadedBackupFiles);
                lastRemovalDuration = java.time.Instant.now().getEpochSecond() - removalBackupStartedTime;
                log.info("Finished DB backup flow");
                lastEntireDuration = java.time.Instant.now().getEpochSecond() - lastBackupStartedTime;
            } catch (CotiRunTimeException e) {
                log.error("Backup DB error.");
                e.logMessage();
                throw e;
            } catch (Exception e) {
                log.error("Backup DB error.\n{}: {}", e.getClass().getName(), e.getMessage());
                throw e;
            } finally {
                backupInProgress.set(false);
                deleteBackup(remoteBackupFolderPath);
            }
        } else {
            String errorMessage = "Backup failed, another backup is in progress! check previous log messages";
            log.error(errorMessage);
            throw new DataBaseBackupException(errorMessage);
        }
    }

    @Override
    public void clearBackupLog() {
        if (isBackup() && !backupInProgress.get() && lastBackupInfo != null) {
            lastBackupInfo = null;
        }
    }

    private void restoreDB() {
        try {
            log.info("Starting DB restore flow");
            if (restoreSource.equals(DbRestoreSource.Local)) {
                log.info("Restoring from local backup");
                databaseConnector.restoreDataBase(localBackupFolderPath);
            } else {
                if (backupToLocalWhenRestoring) {
                    deleteBackup(localBackupFolderPath);
                    databaseConnector.generateDataBaseBackup(localBackupFolderPath);
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
            databaseConnector.restoreDataBase(remoteBackupFolderPath);
        } catch (Exception e) {
            if (e.getCause() != null) {
                log.error("Error while trying to restore DB from Remote:" + e.getCause());
            } else {
                log.error("Error while trying to restore DB from Remote:" + e);
            }

            databaseConnector.restoreDataBase(localBackupFolderPath);
            throw e;
        } finally {
            deleteBackup(remoteBackupFolderPath);
        }
    }

    private String getBackupBucketFromRestoreNode() {

        if (nodeIdentityService.getNodeHash().equals(restoreNodeHash)) {
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
            backupS3Path = sb.toString() + nodeIdentityService.getNodeHash();
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
                if (folderNameArray.length > INDEX_OF_BACKUP_TIMESTAMP_IN_FOLDER_NAME) {
                    s3Backups.add(Long.parseLong(folderNameArray[INDEX_OF_BACKUP_TIMESTAMP_IN_FOLDER_NAME]));
                }
            }
        });
        return s3Backups;
    }

}
