package io.coti.basenode.services;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClientBuilder;
import com.amazonaws.services.identitymanagement.model.GetUserResult;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.services.s3.transfer.*;
import io.coti.basenode.exceptions.AwsDataTransferException;
import io.coti.basenode.exceptions.AwsException;
import io.coti.basenode.services.interfaces.IAwsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.DOCUMENT_NOT_FOUND;

@Service
@Slf4j
public class BaseNodeAwsService implements IAwsService {

    @Value("${aws.s3.bucket.region}")
    private String region;
    @Value("${aws.credentials}")
    protected boolean buildS3ClientWithCredentials;
    protected AmazonS3 s3Client;

    @Override
    public void init() {
        s3Client = getS3Client();
    }

    @Override
    public void createS3Folder(String bucketName, String folderPath) {
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(0);
            InputStream emptyContent = new ByteArrayInputStream(new byte[0]);
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, folderPath + "/", emptyContent, metadata);
            s3Client.putObject(putObjectRequest);
        } catch (Exception e) {
            throw new AwsDataTransferException("Create S3 folder error", e);
        }
    }

    @Override
    public void uploadFolderAndContentsToS3(String bucketName, String s3folderPath, File directoryToUpload) {
        TransferManager transferManager = TransferManagerBuilder.standard().withS3Client(s3Client).build();

        ObjectCannedAclProvider cannedAclProvider = file -> CannedAccessControlList.PublicRead;
        Thread monitorTransferProgress = null;
        try {
            MultipleFileUpload multipleFileUpload = transferManager.uploadDirectory(bucketName, s3folderPath + "/",
                    directoryToUpload, true, null, null, cannedAclProvider);
            monitorTransferProgress = monitorTransferProgress(multipleFileUpload);
            monitorTransferProgress.start();
            multipleFileUpload.waitForCompletion();
            monitorTransferProgress.interrupt();
            monitorTransferProgress.join();
            if (multipleFileUpload.getProgress().getPercentTransferred() == 100) {
                log.debug("Finished uploading files to S3");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AwsDataTransferException("Unable to upload folder and contents to S3. The thread is interrupted");
        } catch (Exception e) {
            throw new AwsDataTransferException("Unable to upload folder and contents to S3.", e);
        } finally {
            if (monitorTransferProgress != null && monitorTransferProgress.isAlive()) {
                monitorTransferProgress.interrupt();
            }
        }
    }

    @Override
    public void downloadFolderAndContents(String bucketName, String s3folderPath, String directoryToDownload) {
        TransferManager transferManager = TransferManagerBuilder.standard().withS3Client(s3Client).build();
        Thread monitorTransferProgress = null;
        try {
            MultipleFileDownload multipleFileDownload = transferManager.downloadDirectory(bucketName, s3folderPath, new File(directoryToDownload));
            monitorTransferProgress = monitorTransferProgress(multipleFileDownload);
            monitorTransferProgress.start();
            multipleFileDownload.waitForCompletion();
            monitorTransferProgress.interrupt();
            monitorTransferProgress.join();
            if (multipleFileDownload.getProgress().getPercentTransferred() == 100) {
                log.debug("Finished downloading files");
            }
            removeExcessFolderStructure(directoryToDownload + "/" + s3folderPath, directoryToDownload);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AwsDataTransferException("Unable to download folder and contents to S3. The thread is interrupted");
        } catch (Exception e) {
            throw new AwsDataTransferException("Unable to download folder and contents to S3.", e);
        } finally {
            if (monitorTransferProgress != null && monitorTransferProgress.isAlive()) {
                monitorTransferProgress.interrupt();
            }
        }
    }

    @Override
    public List<String> listS3Paths(String bucketName, String path) {
        try {
            ListObjectsRequest listObjectsRequest =
                    new ListObjectsRequest()
                            .withBucketName(bucketName)
                            .withPrefix(path + "/");
            List<String> keys = new ArrayList<>();
            ObjectListing objects = s3Client.listObjects(listObjectsRequest);

            List<S3ObjectSummary> summaries = objects.getObjectSummaries();
            summaries.forEach(s -> keys.add(s.getKey()));
            return keys;
        } catch (Exception e) {
            throw new AwsDataTransferException("List S3 paths error.", e);
        }
    }

    @Override
    public void downloadFile(String filePathAndName, String bucketName) {

        String[] filepathDelimited = filePathAndName.split("/");
        String fileName = filepathDelimited[filepathDelimited.length - 1];
        boolean fileExists;
        try {
            fileExists = s3Client.doesObjectExist(bucketName, fileName);
        } catch (Exception e) {
            throw new AwsDataTransferException("S3 check object exist error.", e);
        }

        if (!fileExists) {
            throw new AwsDataTransferException(DOCUMENT_NOT_FOUND);
        }
        File file = new File(filePathAndName);

        try (FileOutputStream fileOutputStream = new FileOutputStream(file);
             BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(fileOutputStream));
             S3Object fullObject = s3Client.getObject(new GetObjectRequest(bucketName, fileName));
             BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fullObject.getObjectContent()))) {

            // Save file locally
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                bufferedWriter.write(line);
                bufferedWriter.newLine();
            }

        } catch (Exception e) {
            throw new AwsDataTransferException("S3 download file error.", e);
        }
    }

    @Override
    public void deleteFolderAndContentsFromS3(List<String> remoteBackups, String bucketName) {
        List<DeleteObjectsRequest.KeyVersion> keysToDelete = new ArrayList<>();
        remoteBackups.forEach(backup -> keysToDelete.add(new DeleteObjectsRequest.KeyVersion(backup)));
        try {
            DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest(bucketName).withKeys(keysToDelete);
            s3Client.deleteObjects(deleteObjectsRequest);
        } catch (Exception e) {
            throw new AwsDataTransferException("Delete folder and contents from S3 error", e);
        }
    }

    @Override
    public void removeExcessFolderStructure(String source, String destination) {
        try {
            String delimiter = "/";
            FileUtils.copyDirectory(new File(source), new File(destination));
            String[] folderHierarchyToDelete = source.split(delimiter);
            FileUtils.deleteDirectory(new File(destination + delimiter + folderHierarchyToDelete[3]));
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    @Override
    public boolean isBuildS3ClientWithCredentials() {
        return buildS3ClientWithCredentials;
    }

    private AmazonS3 getS3Client() {
        try {
            if (buildS3ClientWithCredentials) {
                ProfileCredentialsProvider profileCredentialsProvider = new ProfileCredentialsProvider();
                AmazonIdentityManagement iam = AmazonIdentityManagementClientBuilder.standard().withRegion(region).withCredentials(profileCredentialsProvider).build();
                GetUserResult getUserResult = iam.getUser();
                log.info("Valid credentials for AWS username {}", getUserResult.getUser().getUserName());
                return AmazonS3ClientBuilder.standard().withRegion(region).
                        withCredentials(profileCredentialsProvider).build();
            } else {
                return AmazonS3ClientBuilder.standard().withRegion(region).build();
            }
        } catch (Exception e) {
            throw new AwsException("Get S3 client error", e);
        }
    }

    private Thread monitorTransferProgress(Transfer transfer) {
        return new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    logTransferProgress(transfer);
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    throw new AwsException("Monitor transfer progress error.", e);
                }
            }
            logTransferProgress(transfer);
        });
    }

    private void logTransferProgress(Transfer transfer) {
        TransferProgress progress = transfer.getProgress();
        long bytesTransferred = progress.getBytesTransferred();
        long total = progress.getTotalBytesToTransfer();
        double percentDone = progress.getPercentTransferred();
        log.info("Transfer progress: {}%", (int) percentDone);
        log.info("{} bytes transferred out of {}", bytesTransferred, total);
        Transfer.TransferState transferState = transfer.getState();
        log.info("Transfer state: " + transferState);
    }

}
