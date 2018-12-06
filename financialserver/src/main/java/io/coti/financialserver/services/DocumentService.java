package io.coti.financialserver.services;

import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import io.coti.basenode.data.Hash;
import io.coti.financialserver.data.DisputeData;
import io.coti.financialserver.data.DisputeItemData;
import io.coti.financialserver.database.RocksDBConnector;
import io.coti.financialserver.data.DisputeDocumentData;
import io.coti.financialserver.data.ActionSide;
import io.coti.financialserver.model.Disputes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;
import io.coti.financialserver.model.DisputeDocuments;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@Slf4j
public class DocumentService {

    private static final String OK = "OK";
    private static final String HEADER_CONTENT_DISPOSITION = "Content-Disposition";
    private static final String HEADER_ATTACHMENT_PREFIX = "attachment; filename=";
    private static final String UNAUTHORIZED = "Unauthorized";
    private static final String DOCUMENT_NOT_FOUND = "Document not found";
    private static final String DISPUTE_NOT_FOUND = "Dispute not found";
    private static final String ITEM_NOT_FOUND = "Item not found";
    private AwsService awsService;

    @Autowired
    DisputeDocuments disputeDocuments;

    @Autowired
    Disputes disputes;

    public DocumentService() {
        awsService = new AwsService();

        disputeDocuments = new DisputeDocuments();
        disputeDocuments.init();
        disputeDocuments.databaseConnector = RocksDBConnector.getConnector();

        disputes = new Disputes();
        disputes.init();
        disputes.databaseConnector = RocksDBConnector.getConnector();
    }

    public ResponseEntity newDocument(Hash userHash, Hash disputeHash, Long itemId, String name, String desciption, MultipartFile multiPartFile) {

        DisputeData disputeData = disputes.getByHash(disputeHash);

        if (disputeData == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(DISPUTE_NOT_FOUND);
        }

        ActionSide documentSide = disputeData.getConsumerHash() == userHash ? ActionSide.Consumer : ActionSide.Merchant;

        DisputeDocumentData disputeDocumentData = new DisputeDocumentData(userHash, documentSide, name, desciption);
        DisputeItemData disputeItemData = disputeData.getDisputeItem(itemId);

        if (disputeItemData == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ITEM_NOT_FOUND);
        }

        String documentHashString = disputeDocumentData.getHash().toString();

        disputeItemData.addDocumentHash(disputeDocumentData.getHash());

        disputes.put(disputeData);
        disputeDocuments.put(disputeDocumentData);

        File file = new File(documentHashString);

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

        if ( !awsService.uploadDisputeDocument(disputeDocumentData.getHash(), file, multiPartFile.getOriginalFilename())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(awsService.getError());
        }

        if ( !file.delete() ) {
            log.error("Couldn't delete file: " + documentHashString);
        }

        return ResponseEntity.status(HttpStatus.OK).body(documentHashString);
    }

    public void getDocument(Hash userHash, Hash disputeHash, Long itemId, Hash documentHash, HttpServletResponse response) throws IOException {

        DisputeDocumentData disputeDocument = disputeDocuments.getByHash(documentHash);

        if (disputeDocument == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write(DOCUMENT_NOT_FOUND);
        }
        else if ( !isAuthorized(userHash, disputeHash, itemId, documentHash) ) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(UNAUTHORIZED);
        }
        else {
            response.getWriter().write(disputeDocument.toString());
        }
    }

    public void getDocumentFile(Hash userHash, Hash disputeHash, Long itemId, Hash documentHash, HttpServletResponse response) throws IOException {

        DisputeDocumentData disputeDocument = disputeDocuments.getByHash(documentHash);
        if (disputeDocument == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write(DOCUMENT_NOT_FOUND);
        }
        else if ( !isAuthorized(userHash, disputeHash, itemId, documentHash) ) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(UNAUTHORIZED);
        }
        else {
            try {
                S3ObjectInputStream dis = awsService.getDisputeDocumentInputStream(documentHash.toString());

                response.setHeader(HEADER_CONTENT_DISPOSITION, HEADER_ATTACHMENT_PREFIX + documentHash.toString() + awsService.getSuffix());
                FileCopyUtils.copy(dis, response.getOutputStream());
                dis.close();
            }
            catch (AmazonS3Exception e) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write(DOCUMENT_NOT_FOUND);
            }
        }
    }

    private Boolean isAuthorized(Hash userHash, Hash disputeHash, Long itemId, Hash documentHash) {

        DisputeData disputeData = disputes.getByHash(disputeHash);
        if ( disputeData == null ) {
            return false;
        }

        DisputeItemData disputeItemData = disputeData.getDisputeItem(itemId);
        if ( disputeItemData == null ) {
            return false;
        }

        if( !disputeItemData.getDisputeDocumentHashes().contains(documentHash) ) {
            return false;
        }

        return userHash.equals(disputeData.getConsumerHash()) || userHash.equals(disputeData.getMerchantHash());
    }
}
