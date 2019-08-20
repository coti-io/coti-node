package io.coti.basenode.services;

import io.coti.basenode.database.interfaces.IDatabaseConnector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;

@Slf4j
@Service
public class BaseNodeDBRecoveryService {

    @Value("${db.backup}")
    private boolean backup;
    @Value("${db.backup.bucket}")
    private String bucket;
    @Value("${database.folder.name}")
    private String databaseFolderName;
    @Value("${application.name}")
    private String applicationName;
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
    }

    private void createBackupFolder(String folderPath){
        File directory = new File(folderPath);
        if (!directory.exists()){
            directory.mkdirs();
        }
    }

    private void backupDB() {
        //backup to remote
//        rocksDBConnector.generateDataBaseBackupToRemote();
        rocksDBConnector.generateDataBaseBackup(remoteBackupFolderPath);
        //upload to s3 from backups remote
        //baseNodeAwsService.uploadFile(String sourceFolder);
        //delete backups from remote folder
        deleteBackup(remoteBackupFolderPath);

    }

    private void deleteBackup(String remoteBackupFolderPath) {
        File directoryToBeDeleted = new File(remoteBackupFolderPath);

    }

    private void restoreDB(){
        // backup local db to backups/local
        // download from s3 to backups/remote
        //restore rocksdb from backups/remote
        //delete from backups/remote
    }

}
