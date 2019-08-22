package io.coti.basenode.services;

import io.coti.basenode.data.AddressData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.database.interfaces.IDatabaseConnector;
import io.coti.basenode.model.Addresses;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.List;

@Slf4j
@Service
public class BaseNodeRocksDBRecoveryService {

    @Value("${db.backup}")
    private boolean backup;
    @Value("${db.backup.bucket}")
    private String bucket;
    @Value("${db.backup.local}")
    private boolean backupToLocalWhenRestoring;
    @Value("${database.folder.name}")
    private String databaseFolderName;
    @Value("${application.name}")
    private String applicationName;
    @Value("${network}")
    private String network;
    @Value("${db.restore.hash}")
    private String nodeHash;
    @Autowired
    private IDatabaseConnector rocksDBConnector;
    @Autowired
    private BaseNodeAwsService baseNodeAwsService;
    @Autowired
    private Addresses addresses;
    private String localBackupFolderPath;
    private String remoteBackupFolderPath;
    private String backupNodeHashS3Path;

    @PostConstruct
    private void init(){
        String dbPath = applicationName + databaseFolderName;
        localBackupFolderPath = dbPath + "/backups/local";
        remoteBackupFolderPath = dbPath + "/backups/remote";
        createBackupFolder(localBackupFolderPath);
        createBackupFolder(remoteBackupFolderPath);
        initBackupNodeHashS3Path();
        // 1)
        log.info("Addresses {} empty",addresses.isEmpty() ? "is" : "is not");
        addresses.put(new AddressData(new Hash("aaaaaaaa")));
        log.info("Added address: {}",addresses.getByHash(new Hash("aaaaaaaa")));
        backupDB();

        // 2)
        //restoreDB();
    }

    private void createBackupFolder(String folderPath){
        File directory = new File(folderPath);
        if (!directory.exists()){
            directory.mkdirs();
        }
    }

    private void backupDB() {
        if(!rocksDBConnector.generateDataBaseBackup(remoteBackupFolderPath)){
            return;
            //TODO 8/21/2019 astolia: should implement retry mechanism?
        }
        List<String> backupFolders = baseNodeAwsService.listS3Paths(bucket, backupNodeHashS3Path);
        if(backupFolders.size() == 0){
            baseNodeAwsService.createS3Folder(bucket, backupNodeHashS3Path);
        }
        File backupFolderToUpload = new File(remoteBackupFolderPath);
        baseNodeAwsService.uploadFolderAndContents(bucket, backupNodeHashS3Path, "backup-" + Instant.now().toEpochMilli(), backupFolderToUpload);
        deleteBackup(remoteBackupFolderPath);
        baseNodeAwsService.removeS3PreviousBackup(backupFolders,backupNodeHashS3Path, bucket);
    }

    private void deleteBackup(String backupFolderPath) {
        try {
            FileUtils.cleanDirectory(new File(backupFolderPath));
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private boolean restoreDB(){
        if(backupToLocalWhenRestoring){
            rocksDBConnector.generateDataBaseBackup(localBackupFolderPath);
        }
        // download from s3 to backups/remote
        List<String> backupFolders = baseNodeAwsService.listS3Paths(bucket, backupNodeHashS3Path);
        if(backupFolders.size() == 0){
            log.debug("Couldn't complete restore. No backups found at {}/{}",bucket, backupNodeHashS3Path);
            return false;
            //baseNodeAwsService.createS3Folder(bucket, backupNodeHashS3Path);
        }
//        try {
////            baseNodeAwsService.downloadFile(sb.toString(),bucket); //TODO 8/20/2019 astolia: is this good for downloading folders?
//        } catch (IOException e) {
//            log.error(e.getMessage());
//        }
        rocksDBConnector.restoreDataBase(remoteBackupFolderPath);
        deleteBackup(remoteBackupFolderPath);
        return false; //TODO 8/21/2019 astolia: change
    }

    private void initBackupNodeHashS3Path(){
        String folderDelimiter = "/";
        StringBuilder sb = new StringBuilder(network);
        sb.append(folderDelimiter).append(applicationName).append(folderDelimiter).append(nodeHash);
        backupNodeHashS3Path = sb.toString();
    }

}
