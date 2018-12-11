package io.coti.financialserver.services;

import lombok.extern.slf4j.Slf4j;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.S3ObjectInputStream;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.http.Response;
import io.coti.financialserver.crypto.DocumentCrypto;
import io.coti.financialserver.data.*;
import io.coti.financialserver.http.DocumentRequest;
import io.coti.financialserver.http.NewDocumentResponse;
import io.coti.financialserver.http.GetDocumentResponse;
import io.coti.financialserver.model.Disputes;
import io.coti.financialserver.model.DisputeDocuments;
import static io.coti.basenode.http.BaseNodeHttpStringConstants.STATUS_ERROR;


@Slf4j
@Service
public class DocumentService {

    private static final String HEADER_CONTENT_DISPOSITION = "Content-Disposition";
    private static final String HEADER_ATTACHMENT_PREFIX = "attachment; filename=";
    private static final String UNAUTHORIZED = "Unauthorized";
    private static final String DOCUMENT_NOT_FOUND = "Document not found";
    private static final String DISPUTE_NOT_FOUND = "Dispute not found";
    private static final String ITEM_NOT_FOUND = "Item not found";

    @Autowired
    AwsService awsService;

    @Autowired
    DisputeDocuments disputeDocuments;

    @Autowired
    Disputes disputes;

    public ResponseEntity newDocument(DocumentRequest request) {

        DisputeDocumentData disputeDocumentData = request.getDisputeDocumentData();
        DocumentCrypto documentCrypto = new DocumentCrypto();
        documentCrypto.signMessage(disputeDocumentData);

        if ( !documentCrypto.verifySignature(disputeDocumentData) ) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(UNAUTHORIZED);
        }

        DisputeData disputeData = disputes.getByHash(disputeDocumentData.getDisputeHash());

        if (disputeData == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(DISPUTE_NOT_FOUND);
        }

        DisputeItemData disputeItemData = disputeData.getDisputeItem(disputeDocumentData.getItemId());

        if (disputeItemData == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ITEM_NOT_FOUND);
        }

        ActionSide uploadSide = disputeData.getConsumerHash() == disputeDocumentData.getUserHash() ? ActionSide.Consumer : ActionSide.Merchant;
        disputeDocumentData.setUploadSide(uploadSide);
        disputeDocumentData.init();

        String documentHashString = disputeDocumentData.getHash().toString();

        disputeItemData.addDocumentHash(disputeDocumentData.getHash());

        disputes.put(disputeData);
        disputeDocuments.put(disputeDocumentData);

        return ResponseEntity.status(HttpStatus.OK).body(new NewDocumentResponse(disputeDocumentData.getHash()));
    }

    public ResponseEntity uploadFileToDocument(Hash userHash, Hash disputeHash, Hash documentHash, SignatureData userSignature, MultipartFile multiPartFile) {

        DisputeDocumentData disputeDocumentData = new DisputeDocumentData();
        disputeDocumentData.setHash(documentHash);
        disputeDocumentData.setUserHash(userHash);
        disputeDocumentData.setDisputeHash(disputeHash);
        disputeDocumentData.setSignature(userSignature);
        DocumentCrypto documentCrypto = new DocumentCrypto();
        documentCrypto.signMessage(disputeDocumentData);

        if ( !documentCrypto.verifySignature(disputeDocumentData) ) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(UNAUTHORIZED);
        }

        File file = new File(disputeDocumentData.getHash().toString());

        try {
            if(file.createNewFile()) {
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(multiPartFile.getBytes());
                fos.close();
            }
        }
        catch(IOException e) {
            log.error("Can't save file on disk.", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("OK");
        }

        if ( !awsService.uploadDisputeDocument(disputeDocumentData.getHash(), file, multiPartFile.getOriginalFilename())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(awsService.getError());
        }

        if ( !file.delete() ) {
            log.error("Couldn't delete file: " + documentHash.toString());
        }

        return ResponseEntity.status(HttpStatus.OK).body(new NewDocumentResponse(disputeDocumentData.getHash()));
    }

    public ResponseEntity getDocument(DocumentRequest request) {

        DocumentCrypto disputeCrypto = new DocumentCrypto();
        disputeCrypto.signMessage(request.getDisputeDocumentData());

        if ( !disputeCrypto.verifySignature(request.getDisputeDocumentData()) ) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(UNAUTHORIZED, STATUS_ERROR));
        }
        else {
            DisputeDocumentData disputeDocument = disputeDocuments.getByHash(request.getDisputeDocumentData().getHash());

            if (disputeDocument == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Response(DOCUMENT_NOT_FOUND, STATUS_ERROR));
            } else if (!isAuthorized(request.getDisputeDocumentData().getUserHash(),
                    request.getDisputeDocumentData().getDisputeHash(),
                    request.getDisputeDocumentData().getItemId(),
                    request.getDisputeDocumentData().getHash())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(UNAUTHORIZED, STATUS_ERROR));
            }

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new GetDocumentResponse(disputeDocument));
        }
    }

    public void getDocumentFile(DocumentRequest request, HttpServletResponse response) throws IOException {

        DocumentCrypto disputeCrypto = new DocumentCrypto();
        disputeCrypto.signMessage(request.getDisputeDocumentData());

        if ( !disputeCrypto.verifySignature(request.getDisputeDocumentData()) ) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(UNAUTHORIZED);
        }
        else {
            DisputeDocumentData disputeDocument = disputeDocuments.getByHash(request.getDisputeDocumentData().getHash());
            if (disputeDocument == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write(DOCUMENT_NOT_FOUND);
            } else if (!isAuthorized(request.getDisputeDocumentData().getUserHash(),
                    request.getDisputeDocumentData().getDisputeHash(),
                    request.getDisputeDocumentData().getItemId(),
                    request.getDisputeDocumentData().getHash())) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write(UNAUTHORIZED);
            } else {
                try {
                    S3ObjectInputStream dis = awsService.getDisputeDocumentInputStream(request.getDisputeDocumentData().getHash().toString());

                    response.setHeader(HEADER_CONTENT_DISPOSITION, HEADER_ATTACHMENT_PREFIX + request.getDisputeDocumentData().getHash().toString() + awsService.getSuffix());
                    FileCopyUtils.copy(dis, response.getOutputStream());
                    dis.close();
                } catch (AmazonS3Exception e) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    response.getWriter().write(DOCUMENT_NOT_FOUND);
                }
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
