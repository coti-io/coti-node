package io.coti.basenode.services;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.services.s3.transfer.*;
import io.coti.basenode.exceptions.AwsDataTransferException;
import io.coti.basenode.services.interfaces.IAwsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.*;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.DOCUMENT_NOT_FOUND;
import static io.coti.basenode.http.BaseNodeHttpStringConstants.S3_NOT_REACHABLE;

@Service
@Slf4j
public class BaseNodeAwsService implements IAwsService {

    @Value("${aws.s3.bucket.region}")
    private String region;
    @Value("${aws.credentials}")
    private boolean withCredentials;
    protected AmazonS3 s3Client;

    @Override
    public void init(){
        s3Client = getS3Client();
    }

    protected void createS3Folder(String bucketName, String folderPath){
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(0);
        InputStream emptyContent = new ByteArrayInputStream(new byte[0]);
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, folderPath + "/", emptyContent, metadata);
        s3Client.putObject(putObjectRequest);
    }

    protected void uploadFolderAndContentsToS3(String bucketName, String s3folderPath, File directoryToUpload){
        TransferManager transferManager = TransferManagerBuilder.standard().withS3Client(s3Client).build();

        ObjectCannedAclProvider cannedAclProvider = file -> CannedAccessControlList.PublicRead;
        try {
            MultipleFileUpload multipleFileUpload = transferManager.uploadDirectory(bucketName, s3folderPath + "/",
                    directoryToUpload, true, null, null, cannedAclProvider);

            multipleFileUpload.waitForCompletion();
            if(multipleFileUpload.getProgress().getPercentTransferred() == 100){
                log.debug("Finished uploading files");
            }
        } catch (AmazonServiceException e) {
            log.error(e.getMessage());
            throw new AwsDataTransferException("Unable to upload files.");
        } catch (InterruptedException e){
            log.error(e.getMessage());
            Thread.currentThread().interrupt();
            throw new AwsDataTransferException("Unable to upload files.");
        }
    }

    protected void downloadFolderAndContents(String bucketName, String s3folderPath, String directoryToDownload){
        TransferManager transferManager = TransferManagerBuilder.standard().withS3Client(s3Client).build();
        MultipleFileDownload multipleFileDownload =  transferManager.downloadDirectory(bucketName, s3folderPath, new File(directoryToDownload));
        try {
            multipleFileDownload.waitForCompletion();
            if(multipleFileDownload.getProgress().getPercentTransferred() == 100){
                log.debug("Finished downloading files");
            }
            removeExcessFolderStructure(directoryToDownload + "/" + s3folderPath, directoryToDownload);
        } catch (InterruptedException e) {
            log.error(e.getMessage());
            Thread.currentThread().interrupt();
            throw new AwsDataTransferException("Unable to download files.");
        }
    }

    protected List<String> listS3Paths(String bucketName, String path){
        ListObjectsRequest listObjectsRequest =
                new ListObjectsRequest()
                        .withBucketName(bucketName)
                        .withPrefix(path + "/");
        List<String> keys = new ArrayList<>();
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
            log.error(e.getMessage());
            log.error(S3_NOT_REACHABLE, e);
        } finally {
            if(bufferedReader != null){
                bufferedReader.close();
            }
            // To ensure that the network connection doesn't remain open, close any open input streams.

        }
    }

    protected void deleteFolderAndContentsFromS3(List<String> remoteBackups, String bucketName){
        List<DeleteObjectsRequest.KeyVersion> keysToDelete = new ArrayList<>();
        remoteBackups.forEach(backup -> keysToDelete.add(new DeleteObjectsRequest.KeyVersion(backup)));
        try {
            DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest(bucketName).withKeys(keysToDelete);
            s3Client.deleteObjects(deleteObjectsRequest);
        }
        catch (Exception e){
            log.error(e.getMessage());
        }
    }

    private void removeExcessFolderStructure(String source, String destination){
        try {
            String delimiter = "/";
            FileUtils.copyDirectory(new File(source), new File(destination));
            String[] folderHierarchyToDelete = source.split(delimiter);
            FileUtils.deleteDirectory(new File(destination + delimiter + folderHierarchyToDelete[3]));
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private AmazonS3 getS3Client() {
        if(withCredentials){
            return AmazonS3ClientBuilder.standard().withRegion(region).
                    withCredentials(new ProfileCredentialsProvider()).build();
        }
        else {
            return AmazonS3ClientBuilder.standard().withRegion(region).build();
        }
    }

}
