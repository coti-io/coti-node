package io.coti.financialserver.services;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import io.coti.basenode.data.Hash;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;

import static io.coti.financialserver.http.HttpStringConstants.*;


@Slf4j
@Service
public class AwsService {

    @Value("${aws.s3.bucket.name}")
    private String BUCKET_NAME;

    @Value("${aws.s3.bucket.region}")
    private String REGION;

    @Value("${aws.s3.bucket.name.distribution}")
    private String BUCKET_NAME_DISTRIBUTION;


    private AmazonS3 getS3Client() {
        return AmazonS3ClientBuilder.standard().withRegion(REGION)
                .withCredentials(new ProfileCredentialsProvider())
                .build();
    }

    public String uploadDisputeDocument(Hash documentHash, File file, String contentType) {
        String fileName = documentHash.toString();
        return uploadDocument(file, contentType, fileName, BUCKET_NAME);
    }

    public String uploadDocument(File file, String contentType, String fileName, String bucketName) {
        String error = null;

        if (getS3Client().doesObjectExist(bucketName, fileName)) {
            error = DOCUMENT_EXISTS_ERROR;
        }

        try {
            PutObjectRequest request = new PutObjectRequest(bucketName, fileName, file);
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.addUserMetadata(S3_SUFFIX_METADATA_KEY, contentType);
            request.setMetadata(metadata);
            getS3Client().putObject(request);
        } catch (AmazonServiceException e) {
            log.error(e.getErrorMessage());
            error = DOCUMENT_UPLOAD_ERROR;
        }

        return error;
    }

    public S3Object getS3Object(String fileName) {
        return getS3Client().getObject(BUCKET_NAME, fileName);
    }

    public S3ObjectInputStream downloadFundDistributionFile(String fileName) throws IOException {
        S3Object fullObject = null;

        if (!getS3Client().doesObjectExist(BUCKET_NAME_DISTRIBUTION, fileName)) {
            log.error(DOCUMENT_NOT_FOUND);
            return null;
        }

        try {
            fullObject = getS3Client().getObject(new GetObjectRequest(BUCKET_NAME_DISTRIBUTION, fileName));

            // Save file locally
            File file = new File(fileName);
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(fileOutputStream));
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fullObject.getObjectContent()));
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                bufferedWriter.write(line);
                bufferedWriter.newLine();
            }
            bufferedWriter.close();

            return fullObject.getObjectContent();
        } catch(SdkClientException e) {
            // Amazon S3 couldn't be contacted for a response, or the client
            // couldn't parse the response from Amazon S3.
            e.printStackTrace();
            log.error(S3_NOT_REACHABLE, e);
        }
        finally {
            // To ensure that the network connection doesn't remain open, close any open input streams.
            if(fullObject != null) {
                fullObject.close();
            }
        }
        return null;
    }

    public String uploadFundDistributionResultFile(String fileName, File file, String contentType) {
        return uploadDocument(file, contentType, fileName, BUCKET_NAME_DISTRIBUTION);
    }
}
