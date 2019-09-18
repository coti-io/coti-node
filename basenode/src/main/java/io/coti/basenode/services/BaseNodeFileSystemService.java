package io.coti.basenode.services;

import io.coti.basenode.exceptions.FileSystemException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class BaseNodeFileSystemService {

    public void createFolder(String folderPath) {
        try {
            File folder = new File(folderPath);
            if (!folder.exists()) {
                folder.mkdir();
            }
        } catch (Exception e) {
            throw new FileSystemException(String.format("Create folder error. %s: %s", e.getClass().getName(), e.getMessage()));
        }
    }

    public File[] listFolderFiles(String folderPath) {
        File folder = new File(folderPath);
        return folder.listFiles();
    }

    public List<String> listFolderFileNames(String folderPath) {
        try {
            File folder = new File(folderPath);
            return Arrays.stream(folder.listFiles()).map(file -> file.getName()).collect(Collectors.toList());
        } catch (Exception e) {
            throw new FileSystemException(String.format("List folder file names error. %s: %s", e.getClass().getName(), e.getMessage()));
        }
    }

    public void removeFolderContents(String folderPath) {
        try {
            FileUtils.cleanDirectory(new File(folderPath));
        } catch (Exception e) {
            throw new FileSystemException(String.format("Remove folder contents error. %s: %s", e.getClass().getName(), e.getMessage()));
        }
    }

    public void deleteFile(String filePath) {
        try {
            Files.delete(Paths.get(filePath));
        } catch (Exception e) {
            throw new FileSystemException(String.format("Delete file error. %s: %s", e.getClass().getName(), e.getMessage()));
        }
    }

    public void createFile(String dirPath, String fileName) {
        String fullPath = dirPath + "/" + fileName;
        File file = new File(fullPath);
        try {
            file.createNewFile();
        } catch (IOException e) {
            throw new FileSystemException(String.format("Create file error. %s: %s", e.getClass().getName(), e.getMessage()));
        }
    }

    public void createAndWriteLineToFile(String dirPath, String fileName, String line) {
        String relativePath = dirPath + "/" + fileName;
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(relativePath))) {
            writer.write(line);
        } catch (IOException e) {
            throw new FileSystemException(String.format("Create and write file error. %s: %s", e.getClass().getName(), e.getMessage()));
        }
    }

}
