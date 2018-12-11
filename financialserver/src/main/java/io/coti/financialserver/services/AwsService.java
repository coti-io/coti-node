package io.coti.financialserver.services;

import lombok.extern.slf4j.Slf4j;
import java.io.File;
import org.springframework.stereotype.Service;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.ObjectMetadata;

import io.coti.basenode.data.Hash;

@Slf4j
@Service
public class AwsService {

    private static final String BUCKET_NAME = "financial-server";
    private static final String SUFFIX_METADATA_KEY = "x-amz-meta-suffix";
    private static final String REGION = "us-east-2";
    private static final String DOCUMENT_PREFIX = "document_";
    private static final String SEPARATOR = "_";
    private static final String ERROR_OBJECT_EXIST = "Document already exist.";
    private static final String ERROR_UPLOAD_TO_S3 = "Some error occurred during upload.";

    private String error = "";
    private Object suffix = "";
    private AmazonS3 s3;

    public AwsService() {
        s3 = AmazonS3ClientBuilder.standard().withRegion(REGION)
                .withCredentials(new ProfileCredentialsProvider())
                .build();
    }

    public boolean uploadDisputeDocument(Hash documentHash, File file, String originalName) {
        String fileName = documentHash.toString();

        if(s3.doesObjectExist(BUCKET_NAME, fileName)) {
            error = ERROR_OBJECT_EXIST;
            return false;
        }

        try {
            PutObjectRequest request = new PutObjectRequest(BUCKET_NAME, fileName, file);
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.addUserMetadata(SUFFIX_METADATA_KEY, getUserDocumentSuffix(originalName));
            request.setMetadata(metadata);
            s3.putObject(request);
        } catch (AmazonServiceException e) {
            log.error(e.getErrorMessage());
            error = ERROR_UPLOAD_TO_S3;
        }

        return true;
    }

    public S3ObjectInputStream getDisputeDocumentInputStream(String fileName) {
        S3Object o = s3.getObject(BUCKET_NAME, fileName);
        suffix = o.getObjectMetadata().getUserMetadata().get(SUFFIX_METADATA_KEY);
        return o.getObjectContent();
    }

    public Object getSuffix() {
        return suffix;
    }

    public String getError() {
        return error;
    }

    private String getUserDocumentSuffix(String fileName) {
        String[] splitName = (fileName.split("\\."));
        if(splitName.length > 1) {
            return "." + splitName[splitName.length-1];
        }

        return "";
    }
}
