package io.coti.basenode.services.interfaces;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface IAwsService {

    void init();

    void createS3Folder(String bucketName, String folderPath);

    void uploadFolderAndContentsToS3(String bucketName, String s3folderPath, File directoryToUpload);

    void downloadFolderAndContents(String bucketName, String s3folderPath, String directoryToDownload);

    List<String> listS3Paths(String bucketName, String path);

    void downloadFile(String filePathAndName, String bucketName) throws IOException;

    void deleteFolderAndContentsFromS3(List<String> remoteBackups, String bucketName);

    void removeExcessFolderStructure(String source, String destination);

    boolean isBuildS3ClientWithCredentials();
}
