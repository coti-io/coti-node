package io.coti.basenode.services;

import com.amazonaws.SdkClientException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.DOCUMENT_NOT_FOUND;
import static io.coti.basenode.http.BaseNodeHttpStringConstants.S3_NOT_REACHABLE;

@Slf4j
@Service
public class BaseNodeAwsService implements IAwsService{

    @Value("${aws.s3.bucket.region}")
    private String REGION;

    @Value("${aws.s3.bucket.name.clusterstamp}")
    private String clusterStampBucketName;

    @Value("clusterstamps/")
    private String clusterStampFolder;


    protected AmazonS3 getS3Client() {
        return AmazonS3ClientBuilder.standard().withRegion(REGION)
                .withCredentials(new ProfileCredentialsProvider())
                .build();
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

    @Override
    public void downloadClusterStampFile(String fileName) throws IOException {
        String pathAndFileName = clusterStampFolder + fileName;
        File file = new File(pathAndFileName);
        if(!file.getParentFile().mkdirs()){
            log.error("Failed to create {} folder", file.getParentFile());
        }
        if(!file.createNewFile()){
            log.error("Failed to create {} file", fileName);
        }
        downloadFile(pathAndFileName, clusterStampBucketName);
    }
}
