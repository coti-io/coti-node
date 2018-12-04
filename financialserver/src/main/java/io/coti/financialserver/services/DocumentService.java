package io.coti.financialserver.services;

import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.financialserver.crypto.GetDocumentCrypto;
import io.coti.financialserver.crypto.NewDocumentCrypto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;
import io.coti.financialserver.data.GetDocumentData;
import io.coti.financialserver.data.NewDocumentData;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@Slf4j
public class DocumentService {

    private static final String OK = "OK";
    private static final String NOT_FOUND_BODY = "Document not found";
    private static final String HEADER_CONTENT_DISPOSITION = "Content-Disposition";
    private static final String HEADER_ATTACHMENT_PREFIX = "attachment; filename=";
    private static final String UNAUTHORIZED = "Unauthorized";
    private AwsService awsService;

    public DocumentService() {
        awsService = new AwsService();
    }

    public ResponseEntity newDocument(Hash userHash, Integer disputeId, Integer documentId, MultipartFile multiPartFile, SignatureData signature) {

        NewDocumentData documentData = new NewDocumentData(userHash, disputeId, documentId, signature);
        NewDocumentCrypto documentCrypto = new NewDocumentCrypto();
        documentCrypto.signMessage(documentData);

        if ( !documentCrypto.verifySignature(documentData) ) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(UNAUTHORIZED);
        }

        File file = new File(awsService.getFileName(disputeId, documentId));

        try {
            if(file.createNewFile()) {
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(multiPartFile.getBytes());
                fos.close();
            }
        }
        catch(IOException e) {
            log.error("Can't save file on disk.", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(OK);
        }

        if( !awsService.uploadDisputeDocument(disputeId, documentId, file, multiPartFile.getOriginalFilename())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(awsService.getError());
        }
        if( !file.delete() ) {
            log.error("Couldn't delete file: " + awsService.getFileName(disputeId, documentId));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(OK);
        }

        return ResponseEntity.status(HttpStatus.OK).body(OK);
    }

    public void getDocument(Hash userHash, Integer disputeId, Integer documentId, SignatureData signature, HttpServletResponse response) throws IOException {

        GetDocumentData documentData = new GetDocumentData(userHash, signature);
        GetDocumentCrypto documentCrypto = new GetDocumentCrypto();
        documentCrypto.signMessage(documentData);

        if ( !documentCrypto.verifySignature(documentData) ) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(UNAUTHORIZED);
        }
        else {
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
        }

        response.flushBuffer();
    }
}
