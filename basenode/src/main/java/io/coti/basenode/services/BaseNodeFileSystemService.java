package io.coti.basenode.services;

import io.coti.basenode.exceptions.FileSystemException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class BaseNodeFileSystemService {

    public void createFolder(String folderPath) {
        try {
            File folderPathFile = new File(folderPath);
            if (!folderPathFile.exists()) {
                folderPathFile.mkdir();
            }
        } catch (Exception e) {
            throw new FileSystemException(String.format("Create folder error. Exception: %s, exception message: %s", e.getClass(), e.getMessage()));
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
            throw new FileSystemException(String.format("List folder file names error. Exception: %s, exception message: %s", e.getClass(), e.getMessage()));
        }
    }

    public void removeFolderContents(String folderPath) throws IOException {
        FileUtils.cleanDirectory(new File(folderPath));
    }

}
