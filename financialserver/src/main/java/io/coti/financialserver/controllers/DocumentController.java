package io.coti.financialserver.controllers;

import com.amazonaws.services.s3.model.S3ObjectInputStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.util.FileCopyUtils;
import javax.servlet.http.HttpServletResponse;
import java.io.FileOutputStream;
import io.coti.financialserver.services.AwsService;
import java.io.File;
import java.io.IOException;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Slf4j
@RestController
@RequestMapping("/document")
public class DocumentController {

    private static final String OK = "OK";
    private static final String NOT_FOUND_BODY = "Not found";
    private static final String HEADER_CONTENT_DISPOSITION = "Content-Disposition";
    private static final String HEADER_ATTACHMENT_PREFIX = "attachment; filename=";

    private AwsService awsService;

    public DocumentController() {
        awsService = new AwsService();
    }

    @RequestMapping(method = POST)
    public ResponseEntity handleFileUpload(@RequestParam("disputeId") Integer disputeId,
                                           @RequestParam("documentId") Integer documentId,
                                           @RequestParam("file") MultipartFile multiPartFile) throws IOException {

        File file = new File(awsService.getFileName(disputeId, documentId));
        if(file.createNewFile()) {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(multiPartFile.getBytes());
            fos.close();
        }

        if( !awsService.uploadDisputeDocument(disputeId, documentId, file, multiPartFile.getOriginalFilename())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(awsService.getError());
        }
        if( !file.delete() ) {
            log.error("Couldn't delete file: " + awsService.getFileName(disputeId, documentId));
        }

        return ResponseEntity.status(HttpStatus.OK).body(DocumentController.OK);
    }

    @GetMapping("/{disputeId:.+}/{documentId:.+}")
    public void serveFile(@PathVariable Integer disputeId, @PathVariable Integer documentId, HttpServletResponse response) throws IOException {
        try {
            S3ObjectInputStream dis = awsService.getDisputeDocumentInputStream(disputeId, documentId);

            response.setHeader(HEADER_CONTENT_DISPOSITION, HEADER_ATTACHMENT_PREFIX + awsService.getFileName(disputeId, documentId) + awsService.getSuffix());
            FileCopyUtils.copy(dis, response.getOutputStream());
            dis.close();
        }
        catch (AmazonS3Exception e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write(NOT_FOUND_BODY);
        }

        response.flushBuffer();
    }
}
