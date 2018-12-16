package io.coti.financialserver.services;

import io.coti.financialserver.data.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.Response;
import io.coti.financialserver.crypto.CommentCrypto;
import io.coti.financialserver.http.CommentRequest;
import io.coti.financialserver.http.GetCommentResponse;
import io.coti.financialserver.http.NewCommentResponse;
import io.coti.financialserver.model.DisputeComments;
import io.coti.financialserver.model.Disputes;

import java.util.List;

import static io.coti.financialserver.http.HttpStringConstants.*;

@Slf4j
@Service
public class CommentService {

    @Autowired
    DisputeComments disputeComments;

    @Autowired
    Disputes disputes;

    public ResponseEntity newComment(CommentRequest request) {

        DisputeCommentData disputeCommentData = request.getDisputeCommentData();
        CommentCrypto commentCrypto = new CommentCrypto();
        commentCrypto.signMessage(disputeCommentData);

        if ( !commentCrypto.verifySignature(disputeCommentData) ) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(UNAUTHORIZED, STATUS_ERROR));
        }

        DisputeData disputeData = disputes.getByHash(disputeCommentData.getDisputeHash());

        if (disputeData == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(DISPUTE_NOT_FOUND, STATUS_ERROR));
        }

        List<DisputeItemData> disputeItemsData = disputeData.getDisputeItems(disputeCommentData.getItemIds());

        if (disputeItemsData.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(ITEM_NOT_FOUND, STATUS_ERROR));
        }

        for(DisputeItemData disputeItemData : disputeItemsData) {
            if (disputeItemData.getStatus() != DisputeItemStatus.Recall) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(DISPUTE_ITEM_PASSED_RECALL_STATUS, STATUS_ERROR));
            }

            disputeItemData.addCommentHash(disputeCommentData.getHash());
        }

        ActionSide uploadSide;
        if(disputeData.getConsumerHash().equals(disputeCommentData.getUserHash())) {
            uploadSide = ActionSide.Consumer;
        }
        else if(disputeData.getMerchantHash().equals(disputeCommentData.getUserHash())) {
            uploadSide = ActionSide.Merchant;
        }
        else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(UNAUTHORIZED, STATUS_ERROR));
        }

        disputeCommentData.setCommentSide(uploadSide);
        disputeCommentData.init();

        disputes.put(disputeData);
        disputeComments.put(disputeCommentData);

        return ResponseEntity.status(HttpStatus.OK).body(new NewCommentResponse(disputeCommentData.getHash()));
    }

    public ResponseEntity getComment(CommentRequest request) {

        CommentCrypto disputeCrypto = new CommentCrypto();
        disputeCrypto.signMessage(request.getDisputeCommentData());

        if ( !disputeCrypto.verifySignature(request.getDisputeCommentData()) ) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(UNAUTHORIZED, STATUS_ERROR));
        }
        else {
            DisputeCommentData disputeComment = disputeComments.getByHash(request.getDisputeCommentData().getHash());

            if (disputeComment == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Response(COMMENT_NOT_FOUND, STATUS_ERROR));
            } else if (!isAuthorized(request.getDisputeCommentData().getUserHash(),
                    request.getDisputeCommentData().getDisputeHash(),
                    request.getDisputeCommentData().getItemIds().iterator().next(),
                    request.getDisputeCommentData().getHash())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(UNAUTHORIZED, STATUS_ERROR));
            }

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new GetCommentResponse(disputeComment));
        }
    }

    private Boolean isAuthorized(Hash userHash, Hash disputeHash, Long itemId, Hash commentHash) {

        DisputeData disputeData = disputes.getByHash(disputeHash);
        if ( disputeData == null ) {
            return false;
        }

        DisputeItemData disputeItemData = disputeData.getDisputeItem(itemId);
        if ( disputeItemData == null ) {
            return false;
        }

        if( !disputeItemData.getDisputeCommentHashes().contains(commentHash) ) {
            return false;
        }

        return userHash.equals(disputeData.getConsumerHash()) || userHash.equals(disputeData.getMerchantHash());
    }
}
