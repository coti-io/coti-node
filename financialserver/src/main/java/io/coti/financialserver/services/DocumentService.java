package io.coti.financialserver.services;

import com.amazonaws.services.s3.model.S3Object;
import lombok.extern.slf4j.Slf4j;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

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
import static io.coti.financialserver.http.HttpStringConstants.*;

@Slf4j
@Service
public class DocumentService {

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
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(UNAUTHORIZED, STATUS_ERROR));
        }

        DisputeData disputeData = disputes.getByHash(disputeDocumentData.getDisputeHash());

        if (disputeData == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(DISPUTE_NOT_FOUND, STATUS_ERROR));
        }

        List<DisputeItemData> disputeItemsData = disputeData.getDisputeItems(disputeDocumentData.getItemIds());

        if (disputeItemsData.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(ITEM_NOT_FOUND, STATUS_ERROR));
        }

        ActionSide uploadSide;
        if(disputeData.getConsumerHash().equals(disputeDocumentData.getUserHash())) {
            uploadSide = ActionSide.Consumer;
        }
        else if(disputeData.getMerchantHash().equals(disputeDocumentData.getUserHash())) {
            uploadSide = ActionSide.Merchant;
        }
        else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(UNAUTHORIZED, STATUS_ERROR));
        }

        disputeDocumentData.setUploadSide(uploadSide);
        disputeDocumentData.init();

        for(DisputeItemData disputeItemData : disputeItemsData) {
            disputeItemData.addDocumentHash(disputeDocumentData.getHash());
        }

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
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(UNAUTHORIZED, STATUS_ERROR));
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
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response(INTERNAL_ERROR, STATUS_ERROR));
        }

        if ( !awsService.uploadDisputeDocument(disputeDocumentData.getHash(), file, multiPartFile.getContentType())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(awsService.getError(), STATUS_ERROR));
        }

        if ( !file.delete() ) {
            log.error("Couldn't delete file: " + documentHash.toString());
        }

        disputeDocumentData = disputeDocuments.getByHash(disputeDocumentData.getHash());
        disputeDocumentData.setFileName(multiPartFile.getOriginalFilename());
        disputeDocuments.put(disputeDocumentData);
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
                    request.getDisputeDocumentData().getItemIds().iterator().next(),
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
                    request.getDisputeDocumentData().getItemIds().iterator().next(),
                    request.getDisputeDocumentData().getHash())) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write(UNAUTHORIZED);
            } else {
                try {
                    S3Object s3Object = awsService.getS3Object(request.getDisputeDocumentData().getHash().toString());
                    S3ObjectInputStream dis = s3Object.getObjectContent();
                    response.setHeader(HEADER_CONTENT_DISPOSITION, HEADER_ATTACHMENT_PREFIX + request.getDisputeDocumentData().getHash().toString());
                    response.setHeader(HEADER_CONTENT_TYPE, s3Object.getObjectMetadata().getUserMetadata().get(S3_SUFFIX_METADATA_KEY));
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

        if ( !disputeItemData.getDisputeDocumentHashes().contains(documentHash) ) {
            return false;
        }

        return userHash.equals(disputeData.getConsumerHash()) || userHash.equals(disputeData.getMerchantHash());
    }
}
