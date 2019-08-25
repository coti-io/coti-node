package io.coti.basenode.services;

import io.coti.basenode.crypto.CryptoHelper;
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
import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

@Slf4j
@Service
public class BaseNodeRocksDBRecoveryService {

    private static String[] hexaOptions = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};

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

        // *************************************
        // Testing:
        int addressToStore = 5_000_000;
        generateAndStoreAddressHashes(addressToStore);
//        logWithTimeStart("backupDB(). " + addressToStore + " addresses");
        backupDB();
//        logWithTimeEnd("backupDB()");

        log.info(" *****************************************************************************************");
        logWithTimeStart("restoreDB(). " + addressToStore + " addresses");
        restoreDB();
        logWithTimeEnd("restoreDB()");


        // *************************************
        // 1)
//        log.info("Addresses {} empty",addresses.isEmpty() ? "is" : "is not");
//        addresses.put(new AddressData(new Hash("aaaaaaaa")));
//        log.info("Added address: {}",addresses.getByHash(new Hash("aaaaaaaa")));
//        backupDB();

        // 2)
        //delete
//        restoreDB();
//        log.info("Added address: {}",addresses.getByHash(new Hash("aaaaaaaa")));
    }

    private void createBackupFolder(String folderPath){
        File directory = new File(folderPath);
        if (!directory.exists()){
            directory.mkdirs();
        }
    }

    private void backupDB() {
        logWithTimeStart("generateDataBaseBackup()");
        if(!rocksDBConnector.generateDataBaseBackup(remoteBackupFolderPath)){
            return;
            //TODO 8/21/2019 astolia: should implement retry mechanism?
        }
        logWithTimeEnd("generateDataBaseBackup()");
        List<String> backupFolders = baseNodeAwsService.listS3Paths(bucket, backupNodeHashS3Path);
        if(backupFolders.size() == 0){
            baseNodeAwsService.createS3Folder(bucket, backupNodeHashS3Path);
        }
        File backupFolderToUpload = new File(remoteBackupFolderPath);
        logWithTimeStart("uploadFolderAndContents()");
        baseNodeAwsService.uploadFolderAndContents(bucket, backupNodeHashS3Path + "/backup-" + Instant.now().toEpochMilli(), backupFolderToUpload);
        logWithTimeEnd("uploadFolderAndContents()");
        logWithTimeStart("deleteBackup()");
        deleteBackup(remoteBackupFolderPath);
        logWithTimeEnd("deleteBackup()");
        logWithTimeStart("removeS3PreviousBackup()");
        baseNodeAwsService.removeS3PreviousBackup(backupFolders,backupNodeHashS3Path, bucket);
        logWithTimeEnd("removeS3PreviousBackup()");
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
            logWithTimeStart("generateDataBaseBackup()");
            rocksDBConnector.generateDataBaseBackup(localBackupFolderPath);
            logWithTimeEnd("generateDataBaseBackup()");
        }
        logWithTimeStart("listS3Paths()");
        List<String> s3BackupFolderAndContents = baseNodeAwsService.listS3Paths(bucket, backupNodeHashS3Path);
        logWithTimeEnd("listS3Paths()");
        if(s3BackupFolderAndContents.size() == 0){
            log.debug("Couldn't complete restore. No backups found at {}/{}",bucket, backupNodeHashS3Path);
            return false;
        }
        String latestS3Backup = baseNodeAwsService.getLatestS3Backup(s3BackupFolderAndContents, backupNodeHashS3Path);
//        s3BackupFolderAndContents.removeIf( fromBackupFolder -> !fromBackupFolder.startsWith(latestS3Backup));
        logWithTimeStart("downloadFolderAndContents()");
        baseNodeAwsService.downloadFolderAndContents(bucket, latestS3Backup, remoteBackupFolderPath);
        logWithTimeEnd("downloadFolderAndContents()");
        logWithTimeStart("restoreDataBase()");
        rocksDBConnector.restoreDataBase(remoteBackupFolderPath);
        logWithTimeEnd("restoreDataBase()");
        logWithTimeStart("deleteBackup()");
        deleteBackup(remoteBackupFolderPath);
        logWithTimeEnd("deleteBackup()");
        return true;
    }

    private void initBackupNodeHashS3Path(){
        String folderDelimiter = "/";
        StringBuilder sb = new StringBuilder(network);
        sb.append(folderDelimiter).append(applicationName).append(folderDelimiter).append(nodeHash);
        backupNodeHashS3Path = sb.toString();
    }

    private void generateAndStoreAddressHashes(int addressesToGenerate){
        AddressData addressData;
        for (int i = 0; i <= addressesToGenerate ; i++){
            addressData = new AddressData(generateAddressHash());
            addresses.put(addressData);
        }
    }


    private Hash generateAddressHash(){
        StringBuilder hexa = new StringBuilder();
        for (int i = 0; i < 128; i++) {
            int randomNum = ThreadLocalRandom.current().nextInt(0, 16);
            hexa.append(hexaOptions[randomNum]);
        }
        String generatedPublicKey = hexa.toString();
        byte[] crc32ToAdd = getCrc32OfByteArray(DatatypeConverter.parseHexBinary(generatedPublicKey));
        return new Hash(generatedPublicKey + DatatypeConverter.printHexBinary(crc32ToAdd));
    }

    private static byte[] getCrc32OfByteArray(byte[] array) {
        Checksum checksum = new CRC32();

        byte[] addressWithoutPadding = CryptoHelper.removeLeadingZerosFromAddress(array);
        checksum.update(addressWithoutPadding, 0, addressWithoutPadding.length);
        byte[] checksumValue = ByteBuffer.allocate(4).putInt((int) checksum.getValue()).array();
        return checksumValue;
    }

    private void logWithTimeStart(String msg){
        log.info(" ******** Time: {}. start {}", Instant.now(), msg);
    }

    private void logWithTimeEnd(String msg){
        log.info(" ******** Time: {}. finish {}", Instant.now(), msg);
    }

}
