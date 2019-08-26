package io.coti.basenode.services;

import io.coti.basenode.crypto.NodeCryptoHelper;
import io.coti.basenode.database.interfaces.IDatabaseConnector;
import io.coti.basenode.exceptions.AwsDataTransferException;
import io.coti.basenode.services.interfaces.IDBRecoveryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class BaseNodeDBRecoveryService implements IDBRecoveryService {

    private static final int INDEX_OF_BACKUP_TIMESTAMP_IN_PATH = 3;
    private static final int ALLOWED_NUMBER_OF_BACKUPS = 2;
    @Value("${db.backup}")
    private boolean backup;
    @Value("${db.backup.bucket}")
    private String bucket;
    @Value("${db.backup.local}")
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
    private String nodeHash;
    @Value("${db.restore.source}")
    private String restoreSource; //TODO 8/25/2019 astolia: source of resotre is node(this, other)? or folder(remote,local)?
    @Value("${aws.credentials}")
    private boolean withCredentials;
    @Autowired
    private IDatabaseConnector dBConnector;
    @Autowired
    private BaseNodeAwsService baseNodeAwsService;
    @Autowired
    private NodeCryptoHelper cryptoHelper;
    private String localBackupFolderPath;
    private String remoteBackupFolderPath;
    private String backupS3Path;
    private String restoreS3Path;

    @Override
    public void init(){
        String dbPath = dBConnector.getDBPath();
        localBackupFolderPath = dbPath + "/backups/local";
        remoteBackupFolderPath = dbPath + "/backups/remote";
        createBackupFolder(localBackupFolderPath);
        createBackupFolder(remoteBackupFolderPath);
        initBackupNodeHashS3Path();
        if(restore){
            restoreDB();
        }
    }

    @Scheduled(cron = "${db.backup.time}", zone = "UTC")
    private void backupDB() {
        if(backup && withCredentials){
            if(!dBConnector.generateDataBaseBackup(remoteBackupFolderPath)){
                return;
            }
            List<String> backupFolders = baseNodeAwsService.listS3Paths(bucket, backupS3Path);
            if(backupFolders.isEmpty()){
                baseNodeAwsService.createS3Folder(bucket, backupS3Path);
            }
            File backupFolderToUpload = new File(remoteBackupFolderPath);
            try {
                baseNodeAwsService.uploadFolderAndContentsToS3(bucket, backupS3Path + "/backup-" + Instant.now().toEpochMilli(), backupFolderToUpload);
                deleteBackup(remoteBackupFolderPath);
                if(!backupFolders.isEmpty()){
                    Set<Long> s3Backups = getS3BackupSet(backupFolders);
                    if(s3Backups.size() == ALLOWED_NUMBER_OF_BACKUPS){
                        String backupToRemove = backupS3Path + "/backup-" + Collections.min(s3Backups).toString();
                        backupFolders.removeIf(backup -> backup.startsWith(backupToRemove));
                        baseNodeAwsService.deleteFolderAndContentsFromS3(backupFolders, bucket);
                    }
                }
            } catch (AwsDataTransferException e){
                log.error(e.getMessage());
                deleteBackup(remoteBackupFolderPath);
            }
        }
    }

    private void restoreDB(){
        if(backupToLocalWhenRestoring){
            deleteBackup(localBackupFolderPath);
            dBConnector.generateDataBaseBackup(localBackupFolderPath);
        }
        List<String> s3BackupFolderAndContents = baseNodeAwsService.listS3Paths(bucket, restoreS3Path);
        if(s3BackupFolderAndContents.isEmpty()){
            log.debug("Couldn't complete restore. No backups found at {}/{}",bucket, restoreS3Path);
            return;
        }
        String latestS3Backup = getLatestS3Backup(s3BackupFolderAndContents, restoreS3Path);
        try {
            baseNodeAwsService.downloadFolderAndContents(bucket, latestS3Backup, remoteBackupFolderPath);
            dBConnector.restoreDataBase(remoteBackupFolderPath);
            deleteBackup(remoteBackupFolderPath);
        } catch (AwsDataTransferException e){
            log.error(e.getMessage());
            deleteBackup(remoteBackupFolderPath);
        }
    }

    private void deleteBackup(String backupFolderPath) {
        try {
            FileUtils.cleanDirectory(new File(backupFolderPath));
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void initBackupNodeHashS3Path(){
        String folderDelimiter = "/";
        StringBuilder sb = new StringBuilder(network);
        sb.append(folderDelimiter).append(applicationName).append(folderDelimiter);//.append(nodeHash);
        backupS3Path = sb.toString() + cryptoHelper.getNodeHash();
        restoreS3Path = sb.toString() + nodeHash;
    }

    private void createBackupFolder(String folderPath){
        File directory = new File(folderPath);
        if (!directory.exists()){
            directory.mkdirs();
        }
    }

    private String getLatestS3Backup(List<String> remoteBackups, String backupNodeHashS3Path){
        Set<Long> s3Backups = getS3BackupSet(remoteBackups);
        Long backupTimeStamp = Collections.max(s3Backups);
        return backupNodeHashS3Path + "/backup-" + backupTimeStamp.toString();
    }

    private Set<Long> getS3BackupSet(List<String> remoteBackups){
        String delimiter = "/";
        Set<Long> s3Backups = new HashSet<>();
        remoteBackups.forEach(backup -> {
            String[] backupPathArray = backup.split(delimiter);
            if(backupPathArray.length > INDEX_OF_BACKUP_TIMESTAMP_IN_PATH){
                s3Backups.add(Long.parseLong(backupPathArray[INDEX_OF_BACKUP_TIMESTAMP_IN_PATH].substring(7)));
            }
        });
        return s3Backups;
    }

}
