package io.coti.financialserver.services;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.Response;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.financialserver.crypto.DisputeCommentCrypto;
import io.coti.financialserver.crypto.GetDisputeItemDetailCrypto;
import io.coti.financialserver.data.*;
import io.coti.financialserver.http.GetCommentsRequest;
import io.coti.financialserver.http.GetCommentsResponse;
import io.coti.financialserver.http.NewCommentRequest;
import io.coti.financialserver.http.NewCommentResponse;
import io.coti.financialserver.http.data.GetDisputeItemDetailData;
import io.coti.financialserver.model.DisputeComments;
import io.coti.financialserver.model.Disputes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static io.coti.financialserver.http.HttpStringConstants.*;

@Slf4j
@Service
public class CommentService {

    @Autowired
    private DisputeComments disputeComments;
    @Autowired
    private DisputeCommentCrypto disputeCommentCrypto;
    @Autowired
    private GetDisputeItemDetailCrypto getDisputeCommentsCrypto;
    @Autowired
    private Disputes disputes;
    @Autowired
    private DisputeService disputeService;

    public ResponseEntity<IResponse> newComment(NewCommentRequest request) {

        DisputeCommentData disputeCommentData = request.getDisputeCommentData();
        disputeCommentData.init();

        if (!disputeCommentCrypto.verifySignature(disputeCommentData)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(INVALID_SIGNATURE, STATUS_ERROR));
        }

        DisputeData disputeData = disputes.getByHash(disputeCommentData.getDisputeHash());

        if (disputeData == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(DISPUTE_NOT_FOUND, STATUS_ERROR));
        }

        List<DisputeItemData> disputeItemsData = disputeData.getDisputeItems(disputeCommentData.getItemIds());

        if (disputeItemsData.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(ITEM_NOT_FOUND, STATUS_ERROR));
        }

        for (DisputeItemData disputeItemData : disputeItemsData) {
            if (disputeItemData.getStatus() != DisputeItemStatus.Recall) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(DISPUTE_ITEM_PASSED_RECALL_STATUS, STATUS_ERROR));
            }

            disputeItemData.addCommentHash(disputeCommentData.getHash());
        }

        ActionSide uploadSide;
        if (disputeData.getConsumerHash().equals(disputeCommentData.getUserHash())) {
            uploadSide = ActionSide.Consumer;
        } else if (disputeData.getMerchantHash().equals(disputeCommentData.getUserHash())) {
            uploadSide = ActionSide.Merchant;
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(DISPUTE_COMMENT_CREATE_UNAUTHORIZED, STATUS_ERROR));
        }

        disputeCommentData.setCommentSide(uploadSide);

        disputes.put(disputeData);
        disputeComments.put(disputeCommentData);

        return ResponseEntity.status(HttpStatus.OK).body(new NewCommentResponse(disputeCommentData.getHash()));
    }

    public ResponseEntity<IResponse> getComments(GetCommentsRequest request) {
        GetDisputeItemDetailData getDisputeCommentsData = request.getDisputeCommentsData();

        if (!getDisputeCommentsCrypto.verifySignature(getDisputeCommentsData)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(INVALID_SIGNATURE, STATUS_ERROR));
        }

        DisputeData disputeData = disputes.getByHash(getDisputeCommentsData.getDisputeHash());
        if (disputeData == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(DISPUTE_NOT_FOUND, STATUS_ERROR));
        }
        if (!disputeService.isAuthorizedDisputeDetailDisplay(disputeData, getDisputeCommentsData.getUserHash())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(DISPUTE_COMMENT_UNAUTHORIZED, STATUS_ERROR));
        }
        DisputeItemData disputeItemData = disputeData.getDisputeItems().stream().filter(disputeItem -> disputeItem.getId().equals(getDisputeCommentsData.getItemId())).findFirst().get();
        if (disputeItemData == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(DISPUTE_ITEM_NOT_FOUND, STATUS_ERROR));
        }
        List<Hash> disputeCommentHashes = disputeItemData.getDisputeCommentHashes() != null ? disputeItemData.getDisputeCommentHashes() : new ArrayList<>();
        List<DisputeCommentData> disputeCommentDataList = new ArrayList<>();
        disputeCommentHashes.forEach(disputeCommentHash -> disputeCommentDataList.add(disputeComments.getByHash(disputeCommentHash)));

        return ResponseEntity.status(HttpStatus.OK).body(new GetCommentsResponse(disputeCommentDataList));

    }
}
