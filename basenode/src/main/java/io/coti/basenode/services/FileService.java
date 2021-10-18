package io.coti.basenode.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

@Slf4j
@Service
public class FileService {

    public void writeToFile(MultipartFile multiPartFile, File file) throws IOException {
        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            fileOutputStream.write(multiPartFile.getBytes());
        }
    }

    public void deleteFile(File file) {
        try {
            Files.delete(file.toPath());
        } catch (IOException e) {
            log.error("Couldn't delete file");
            log.error(e.getMessage());
        }
    }
}
