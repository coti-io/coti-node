package io.coti.basenode.services;

import io.coti.basenode.database.interfaces.IDatabaseConnector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
    private String localBackupFolderPath;
    private String remoteBackupFolderPath;

    @PostConstruct
    private void init(){
        String dbPath = applicationName + databaseFolderName;
        localBackupFolderPath = dbPath + "/backups/local";
        remoteBackupFolderPath = dbPath + "/backups/remote";
        createBackupFolder(localBackupFolderPath);
        createBackupFolder(remoteBackupFolderPath);
        deleteBackup(remoteBackupFolderPath);
    }

    private void createBackupFolder(String folderPath){
        File directory = new File(folderPath);
        if (!directory.exists()){
            directory.mkdirs();
        }
    }

    private void backupDB() {
        rocksDBConnector.generateDataBaseBackup(remoteBackupFolderPath);
        //upload to s3 from backups remote
        //baseNodeAwsService.uploadFile(String sourceFolder);
        deleteBackup(remoteBackupFolderPath);

    }

    private void deleteBackup(String backupFolderPath) {
        try {
            Files.walk(Paths.get(backupFolderPath))
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void restoreDB(){
        if(backupToLocalWhenRestoring){
            rocksDBConnector.generateDataBaseBackup(localBackupFolderPath);
        }
        // download from s3 to backups/remote
        String folderDelimiter = "/";
        StringBuilder sb = new StringBuilder(network);
        sb.append(folderDelimiter).append(applicationName).append(folderDelimiter).append(nodeHash);
        try {
            baseNodeAwsService.downloadFile(sb.toString(),bucket); //TODO 8/20/2019 astolia: is this good for downloading folders?
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        rocksDBConnector.restoreDataBase(remoteBackupFolderPath);
        deleteBackup(remoteBackupFolderPath);
    }

}
