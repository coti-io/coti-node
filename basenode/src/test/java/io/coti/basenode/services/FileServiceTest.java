package io.coti.basenode.services;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

@ContextConfiguration(classes = {FileService.class})
@TestPropertySource(locations = "classpath:test.properties")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ExtendWith(OutputCaptureExtension.class)
@SpringBootTest
@ExtendWith(SpringExtension.class)
class FileServiceTest {
    @Autowired
    private FileService fileService;

    @Test
    @Order(1)
    void testWriteToFile() throws IOException {
        MockMultipartFile multiPartFile = new MockMultipartFile("Name",
                new ByteArrayInputStream("AAAAAAAA".getBytes(StandardCharsets.UTF_8)));
        fileService.writeToFile(multiPartFile, Paths.get(System.getProperty("java.io.tmpdir"), "test.txt").toFile());
        assertNotNull(Paths.get(System.getProperty("java.io.tmpdir"), "test.txt"));
    }

    @Test
    @Order(2)
    void testDeleteFile(CapturedOutput output) {
        fileService.deleteFile(Paths.get(System.getProperty("java.io.tmpdir"), "test.txt").toFile());
        assertFalse(output.getOut().contains("Couldn't delete file"));
    }

    @Test
    @Order(3)
    void testDeleteFile_could_not_delete_file(CapturedOutput output) {
        fileService.deleteFile(Paths.get(System.getProperty("AAAAAAAA"), "test.txt").toFile());
        assertTrue(output.getOut().contains("Couldn't delete file"));
    }
}

