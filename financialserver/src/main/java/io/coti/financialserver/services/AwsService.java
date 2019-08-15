package io.coti.financialserver.services;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import io.coti.basenode.data.Hash;
import io.coti.basenode.services.BaseNodeAwsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

import static io.coti.financialserver.http.HttpStringConstants.*;
import static io.coti.financialserver.services.InjectionService.BUCKET_NAME;
import static io.coti.financialserver.services.InjectionService.BUCKET_NAME_DISTRIBUTION;

@Slf4j
@Service
public class AwsService extends BaseNodeAwsService {


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

    public void downloadFundDistributionFile(String fileName) throws IOException {
        downloadFile(fileName, BUCKET_NAME_DISTRIBUTION);
    }

    public String uploadFundDistributionResultFile(String fileName, File file, String contentType) {
        return uploadDocument(file, contentType, fileName, BUCKET_NAME_DISTRIBUTION);
    }
}
