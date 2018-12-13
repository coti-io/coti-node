package io.coti.financialserver.services;

import lombok.extern.slf4j.Slf4j;
import java.io.File;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.ObjectMetadata;
import io.coti.basenode.data.Hash;
import static io.coti.financialserver.http.HttpStringConstants.*;


@Slf4j
@Service
public class AwsService {

    @Value("${aws.s3.bucket.name}")
    private String BUCKET_NAME;

    @Value("${aws.s3.bucket.region}")
    private String REGION;

    private static final String ERROR_OBJECT_EXIST = "Document already exist.";
    private static final String ERROR_UPLOAD_TO_S3 = "Some error occurred during upload.";

    private String error = "";

    private AmazonS3 getS3Client() {
        return AmazonS3ClientBuilder.standard().withRegion(REGION)
                .withCredentials(new ProfileCredentialsProvider())
                .build();
    }

    public boolean uploadDisputeDocument(Hash documentHash, File file, String contentType) {
        String fileName = documentHash.toString();

        if(getS3Client().doesObjectExist(BUCKET_NAME, fileName)) {
            error = ERROR_OBJECT_EXIST;
            return false;
        }

        try {
            PutObjectRequest request = new PutObjectRequest(BUCKET_NAME, fileName, file);
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.addUserMetadata(S3_SUFFIX_METADATA_KEY, contentType);
            request.setMetadata(metadata);
            getS3Client().putObject(request);
        } catch (AmazonServiceException e) {
            log.error(e.getErrorMessage());
            error = ERROR_UPLOAD_TO_S3;
        }

        return true;
    }

    public S3Object getS3Object(String fileName) {
        return getS3Client().getObject(BUCKET_NAME, fileName);
    }

    public String getError() {
        return error;
    }
}
