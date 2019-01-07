package io.coti.financialserver.services;

import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import io.coti.basenode.data.Hash;
import io.coti.basenode.http.Response;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.financialserver.crypto.DisputeDocumentCrypto;
import io.coti.financialserver.crypto.GetDisputeItemDetailCrypto;
import io.coti.financialserver.crypto.GetDocumentFileCrypto;
import io.coti.financialserver.data.*;
import io.coti.financialserver.http.*;
import io.coti.financialserver.http.data.GetDisputeItemDetailData;
import io.coti.financialserver.http.data.GetDocumentFileData;
import io.coti.financialserver.model.DisputeDocuments;
import io.coti.financialserver.model.Disputes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static io.coti.financialserver.http.HttpStringConstants.*;

@Slf4j
@Service
public class DocumentService {

    @Autowired
    private AwsService awsService;
    @Autowired
    private DisputeDocuments disputeDocuments;
    @Autowired
    private DisputeDocumentCrypto disputeDocumentCrypto;
    @Autowired
    private GetDisputeItemDetailCrypto getDisputeDocumentNamesCrypto;
    @Autowired
    private GetDocumentFileCrypto getDocumentFileCrypto;
    @Autowired
    private Disputes disputes;
    @Autowired
    private DisputeService disputeService;
    @Autowired
    private WebSocketService webSocketService;

    public ResponseEntity<IResponse> newDocument(NewDocumentRequest request) {

        DisputeDocumentData disputeDocumentData = request.getDisputeDocumentData();

        if (!disputeDocumentCrypto.verifySignature(disputeDocumentData)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(INVALID_SIGNATURE, STATUS_ERROR));
        }

        DisputeData disputeData = disputes.getByHash(disputeDocumentData.getDisputeHash());

        if (disputeData == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(DISPUTE_NOT_FOUND, STATUS_ERROR));
        }

        List<DisputeItemData> disputeItemsData = disputeData.getDisputeItems(disputeDocumentData.getItemIds());

        if (disputeItemsData.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(ITEM_NOT_FOUND, STATUS_ERROR));
        }

        for (DisputeItemData disputeItemData : disputeItemsData) {
            if (disputeItemData.getStatus() != DisputeItemStatus.Recall) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(DISPUTE_ITEM_PASSED_RECALL_STATUS, STATUS_ERROR));
            }

            disputeItemData.addDocumentHash(disputeDocumentData.getHash());
        }

        ActionSide actionSide = disputeService.getActionSide(disputeData, disputeDocumentData.getUserHash());
        if (actionSide == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(DISPUTE_COMMENT_CREATE_UNAUTHORIZED, STATUS_ERROR));
        }

        disputeDocumentData.setUploadSide(actionSide);

        MultipartFile multiPartFile = request.getFile();

        File file = new File(disputeDocumentData.getHash().toString());

        try {
            if (file.createNewFile()) {
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                fileOutputStream.write(multiPartFile.getBytes());
                fileOutputStream.close();
            }
        } catch (IOException e) {
            log.error("Can't save file on disk.", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response(INTERNAL_ERROR, STATUS_ERROR));
        }

        String upload_error = awsService.uploadDisputeDocument(disputeDocumentData.getHash(), file, multiPartFile.getContentType());
        if (upload_error != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(upload_error, STATUS_ERROR));
        }

        if (!file.delete()) {
            log.error("Couldn't delete file: {}", disputeDocumentData.getHash());
        }

        disputeDocumentData.setFileName(multiPartFile.getOriginalFilename());

        disputes.put(disputeData);
        disputeDocuments.put(disputeDocumentData);

        webSocketService.notifyOnNewCommentOrDocument(disputeData, disputeDocumentData, disputeDocumentData.getUploadSide());
        return ResponseEntity.status(HttpStatus.OK).body(new NewDocumentResponse(disputeDocumentData));
    }

    public ResponseEntity<IResponse> getDocumentNames(GetDocumentNamesRequest request) {
        GetDisputeItemDetailData getDisputeDocumentNamesData = request.getDisputeDocumentNamesData();

        if (!getDisputeDocumentNamesCrypto.verifySignature(getDisputeDocumentNamesData)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(INVALID_SIGNATURE, STATUS_ERROR));
        }

        DisputeData disputeData = disputes.getByHash(getDisputeDocumentNamesData.getDisputeHash());
        if (disputeData == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(DISPUTE_NOT_FOUND, STATUS_ERROR));
        }
        if (!disputeService.isAuthorizedDisputeDetailDisplay(disputeData, getDisputeDocumentNamesData.getUserHash())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(DISPUTE_DOCUMENT_UNAUTHORIZED, STATUS_ERROR));
        }
        DisputeItemData disputeItemData = disputeData.getDisputeItem(getDisputeDocumentNamesData.getItemId());
        if (disputeItemData == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(DISPUTE_ITEM_NOT_FOUND, STATUS_ERROR));
        }

        List<Hash> disputeDocumentHashes = disputeItemData.getDisputeDocumentHashes() != null ? disputeItemData.getDisputeDocumentHashes() : new ArrayList<>();
        List<DisputeDocumentData> disputeDocumentDataList = new ArrayList<>();
        disputeDocumentHashes.forEach(disputeDocumentHash -> disputeDocumentDataList.add(disputeDocuments.getByHash(disputeDocumentHash)));

        return ResponseEntity.status(HttpStatus.OK).body(new GetDocumentNamesResponse(disputeDocumentDataList));
    }

    public void getDocumentFile(GetDocumentFileRequest request, HttpServletResponse response) throws IOException {
        GetDocumentFileData getDocumentFileData = request.getDocumentFileData();

        if (!getDocumentFileCrypto.verifySignature(getDocumentFileData)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(INVALID_SIGNATURE);
            return;
        }

        DisputeDocumentData disputeDocumentData = disputeDocuments.getByHash(getDocumentFileData.getDocumentHash());
        if (disputeDocumentData == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(DOCUMENT_NOT_FOUND);
            return;
        }
        DisputeData disputeData = disputes.getByHash(disputeDocumentData.getDisputeHash());
        if (disputeData == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(DISPUTE_NOT_FOUND);
            return;
        }
        if (!disputeService.isAuthorizedDisputeDetailDisplay(disputeData, getDocumentFileData.getUserHash())) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(DISPUTE_DOCUMENT_UNAUTHORIZED);
            return;
        }

        try {
            S3Object s3Object = awsService.getS3Object(getDocumentFileData.getDocumentHash().toString());
            S3ObjectInputStream inputStream = s3Object.getObjectContent();
            response.setHeader(HEADER_CONTENT_DISPOSITION, HEADER_ATTACHMENT_PREFIX + getDocumentFileData.getDocumentHash().toString());
            response.setHeader(HEADER_CONTENT_TYPE, s3Object.getObjectMetadata().getUserMetadata().get(S3_SUFFIX_METADATA_KEY));
            FileCopyUtils.copy(inputStream, response.getOutputStream());
            inputStream.close();
        } catch (AmazonS3Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(DOCUMENT_NOT_FOUND);
        }
    }
}
