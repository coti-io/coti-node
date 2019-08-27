package io.coti.financialserver.services;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import io.coti.basenode.data.Hash;
import io.coti.basenode.exceptions.AwsException;
import io.coti.basenode.services.BaseNodeAwsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;

import static io.coti.financialserver.http.HttpStringConstants.*;


@Slf4j
@Service
public class AwsService extends BaseNodeAwsService {

    @Value("${aws.s3.bucket.name}")
    private String BUCKET_NAME;
    @Value("${aws.s3.bucket.name.distribution}")
    private String BUCKET_NAME_DISTRIBUTION;

    @Override
    public void init() {
        if (!buildS3ClientWithCredentials) {
            throw new AwsException("AWS S3 client with credentials should be set to true");
        }
        super.init();
    }

    public String uploadDisputeDocument(Hash documentHash, File file, String contentType) {
        String fileName = documentHash.toString();
        return uploadDocument(file, contentType, fileName, BUCKET_NAME);
    }

    public String uploadDocument(File file, String contentType, String fileName, String bucketName) {
        String error = null;

        if (s3Client.doesObjectExist(bucketName, fileName)) {
            error = DOCUMENT_EXISTS_ERROR;
        }

        try {
            PutObjectRequest request = new PutObjectRequest(bucketName, fileName, file);
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.addUserMetadata(S3_SUFFIX_METADATA_KEY, contentType);
            request.setMetadata(metadata);
            s3Client.putObject(request);
        } catch (AmazonServiceException e) {
            log.error(e.getErrorMessage());
            error = DOCUMENT_UPLOAD_ERROR;
        }

        return error;
    }

    public S3Object getS3Object(String fileName) {
        return s3Client.getObject(BUCKET_NAME, fileName);
    }

    public void downloadFundDistributionFile(String fileName) {
        downloadFile(fileName, BUCKET_NAME_DISTRIBUTION);
    }

    public String uploadFundDistributionResultFile(String fileName, File file, String contentType) {
        return uploadDocument(file, contentType, fileName, BUCKET_NAME_DISTRIBUTION);
    }
}
