package io.coti.basenode.services;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.services.s3.transfer.MultipleFileDownload;
import com.amazonaws.services.s3.transfer.MultipleFileUpload;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.*;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.DOCUMENT_NOT_FOUND;
import static io.coti.basenode.http.BaseNodeHttpStringConstants.S3_NOT_REACHABLE;

@Service
@Slf4j
public class BaseNodeAwsService {

    private static final int ALLOWED_NUMBER_OF_BACKUPS = 2;
    private static final int INDEX_OF_BACKUP_TIMESTAMP_IN_PATH = 3;
    @Value("${aws.s3.bucket.region}")
    private String REGION;
    private AmazonS3 s3Client;

    @PostConstruct
    private void init(){
        s3Client = getS3Client();
    }


    protected AmazonS3 getS3Client() {
        return AmazonS3ClientBuilder.standard().withRegion(REGION)
                .withCredentials(new ProfileCredentialsProvider())
                .build();
    }

    protected void createS3Folder(String bucketName, String folderPath){
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(0);
        InputStream emptyContent = new ByteArrayInputStream(new byte[0]);
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, folderPath + "/", emptyContent, metadata);
        s3Client.putObject(putObjectRequest);
    }

    protected void uploadFolderAndContents(String bucketName, String s3folderPath, File directoryToUpload){
        createS3Folder(bucketName,s3folderPath);
        TransferManager transferManager = TransferManagerBuilder.standard().withS3Client(s3Client).build();

        try {
            MultipleFileUpload multipleFileUpload = transferManager.uploadDirectory(bucketName,
                    s3folderPath + "/", directoryToUpload, true);
            multipleFileUpload.waitForCompletion();
            if(multipleFileUpload.getProgress().getPercentTransferred() == 100){
                log.debug("Finished uploading database backup to s3");
            }
        } catch (AmazonServiceException e) {
            log.error(e.getMessage());
        } catch (InterruptedException e) {
            log.error(e.getMessage());
        } catch (Exception e){
            log.error(e.getMessage());
        }
    }

    protected void downloadFolderAndContents(String bucketName, String s3folderPath, String directoryToDownload){
        TransferManager transferManager = TransferManagerBuilder.standard().withS3Client(s3Client).build();
        MultipleFileDownload multipleFileDownload =  transferManager.downloadDirectory(bucketName, s3folderPath, new File(directoryToDownload));
        try {
            multipleFileDownload.waitForCompletion();
            if(multipleFileDownload.getProgress().getPercentTransferred() == 100){
                log.debug("Finished downloading database backup to s3");
            }
            removeExcessFolderStructure(directoryToDownload + "/" + s3folderPath, directoryToDownload);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void removeExcessFolderStructure(String source, String destination){
        try {
            FileUtils.copyDirectory(new File(source), new File(destination));
            String[] folderHierarchyToDelete = source.split("/");
            FileUtils.deleteDirectory(new File(destination+ "/" + folderHierarchyToDelete[3]));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected List<String> listS3Paths(String bucketName, String path){
        ListObjectsRequest listObjectsRequest =
                new ListObjectsRequest()
                        .withBucketName(bucketName)
                        .withPrefix(path + "/");
        List<String> keys = new ArrayList<>();
        AmazonS3 s3Client = getS3Client();
        ObjectListing objects = s3Client.listObjects(listObjectsRequest);

        List<S3ObjectSummary> summaries = objects.getObjectSummaries();
        summaries.forEach(s -> keys.add(s.getKey()));
        return keys;
    }

    protected void downloadFile(String filePathAndName, String bucketName) throws IOException {
        S3Object fullObject;

        String[] filepathDelimited = filePathAndName.split("/");
        String fileName = filepathDelimited[filepathDelimited.length - 1];

        if (!getS3Client().doesObjectExist(bucketName, fileName)) {
            log.error(DOCUMENT_NOT_FOUND);
        }
        File file = new File(filePathAndName);

        BufferedReader bufferedReader = null;
        try (FileOutputStream fileOutputStream = new FileOutputStream(file);
             BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(fileOutputStream))){
            fullObject = getS3Client().getObject(new GetObjectRequest(bucketName, fileName));
            bufferedReader = new BufferedReader(new InputStreamReader(fullObject.getObjectContent()));
            // Save file locally

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                bufferedWriter.write(line);
                bufferedWriter.newLine();
            }

        } catch (SdkClientException e) {
            // Amazon S3 couldn't be contacted for a response, or the client
            // couldn't parse the response from Amazon S3.
            e.printStackTrace();
            log.error(S3_NOT_REACHABLE, e);
        } finally {
            if(bufferedReader != null){
                bufferedReader.close();
            }
            // To ensure that the network connection doesn't remain open, close any open input streams.

        }
    }

    protected void removeS3PreviousBackup(List<String> remoteBackups, String backupNodeHashS3Path, String bucketName){
        if(remoteBackups.isEmpty()){
            return;
        }
        Set<Long> s3Backups = getS3BackupSet(remoteBackups);
        if(s3Backups.size() != ALLOWED_NUMBER_OF_BACKUPS){
            return;
        }
        String backupToRemove = backupNodeHashS3Path + "/backup-" + Collections.min(s3Backups).toString();
        List<DeleteObjectsRequest.KeyVersion> keysToDelete = new ArrayList<>();
        remoteBackups.forEach(backup -> {
            if(backup.startsWith(backupToRemove)){
                keysToDelete.add(new DeleteObjectsRequest.KeyVersion(backup));
            }
        });
        try {
            log.debug("deleting {}/{}", bucketName, backupToRemove);
            DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest(bucketName).withKeys(keysToDelete);
            s3Client.deleteObjects(deleteObjectsRequest);
        }
        catch (Exception e){
            log.error(e.getMessage());
        }
    }

    protected String getLatestS3Backup(List<String> remoteBackups, String backupNodeHashS3Path){
        Set<Long> s3Backups = getS3BackupSet(remoteBackups);
        Long backupTimeStamp = Collections.max(s3Backups);
        return backupNodeHashS3Path + "/backup-" + backupTimeStamp.toString();
    }

    private Set<Long> getS3BackupSet(List<String> remoteBackups){
        Set<Long> s3Backups = new HashSet<>();
        remoteBackups.forEach(backup -> {
            String[] backupPathArray = backup.split("/");
            if(backupPathArray.length > INDEX_OF_BACKUP_TIMESTAMP_IN_PATH){
                s3Backups.add(Long.parseLong(backupPathArray[INDEX_OF_BACKUP_TIMESTAMP_IN_PATH].substring(7)));
            }
        });
        return s3Backups;
    }

}
