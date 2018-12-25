package io.coti.financialserver.services;

import io.coti.basenode.http.Response;
import io.coti.financialserver.crypto.UpdateItemCrypto;
import io.coti.financialserver.data.*;
import io.coti.financialserver.http.UpdateItemRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import io.coti.financialserver.crypto.ItemCrypto;
import io.coti.financialserver.crypto.ItemVoteCrypto;
import io.coti.financialserver.http.ItemRequest;
import io.coti.financialserver.http.VoteRequest;
import io.coti.financialserver.model.Disputes;

import java.util.ArrayList;
import java.util.List;

import static io.coti.financialserver.http.HttpStringConstants.*;

@Slf4j
@Service
public class ItemService {

    @Autowired
    Disputes disputes;

    @Autowired
    DisputeService disputeService;

    public ResponseEntity newItem(ItemRequest request) {

        DisputeItemData disputeItemData = request.getDisputeItemData();
        ItemCrypto itemCrypto = new ItemCrypto();
        itemCrypto.signMessage(disputeItemData);

        if ( !itemCrypto.verifySignature(disputeItemData) ) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(UNAUTHORIZED, STATUS_ERROR));
        }

        DisputeData disputeData = disputes.getByHash(disputeItemData.getDisputeHash());

        if (disputeData == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(DISPUTE_NOT_FOUND, STATUS_ERROR));
        }

        if( !disputeData.getConsumerHash().equals(disputeItemData.getUserHash()) ) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(UNAUTHORIZED, STATUS_ERROR));
        }

        List<DisputeItemData> disputeItems = new ArrayList<>();
        disputeItems.add(disputeItemData);
        if ( !disputeService.isDisputeItemsValid(disputeData.getConsumerHash(), disputeItems, disputeData.getTransactionHash()) ) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(DISPUTE_ITEMS_EXIST_ALREADY, STATUS_ERROR));
        }

        disputeItemData.setStatus(DisputeItemStatus.Recall);
        disputeData.getDisputeItems().add(disputeItemData);
        disputeService.update(disputeData);

        return ResponseEntity.status(HttpStatus.OK).body(new Response(SUCCESS, STATUS_SUCCESS));
    }

    public ResponseEntity updateItem(UpdateItemRequest request) {

        DisputeUpdateItemData disputeUpdateItemData = request.getDisputeUpdateItemData();
        UpdateItemCrypto updateItemCrypto = new UpdateItemCrypto();
        updateItemCrypto.signMessage(disputeUpdateItemData);

        if ( !updateItemCrypto.verifySignature(disputeUpdateItemData) ) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(UNAUTHORIZED, STATUS_ERROR));
        }

        DisputeData disputeData = disputes.getByHash(disputeUpdateItemData.getDisputeHash());

        if (disputeData == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(DISPUTE_NOT_FOUND, STATUS_ERROR));
        }

        ActionSide actionSide;
        if(disputeData.getConsumerHash().equals(disputeUpdateItemData.getUserHash())) {
            actionSide = ActionSide.Consumer;
        }
        else if(disputeData.getMerchantHash().equals(disputeUpdateItemData.getUserHash())) {
            actionSide = ActionSide.Merchant;
        }
        else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(UNAUTHORIZED, STATUS_ERROR));
        }

        for(Long itemId : disputeUpdateItemData.getIds()) {

            DisputeItemData disputeItemData = disputeData.getDisputeItem(itemId);

            if (disputeItemData == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(ITEM_NOT_FOUND, STATUS_ERROR));
            }

            if (disputeItemData.getStatus() != DisputeItemStatus.Recall) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(DISPUTE_ITEM_PASSED_RECALL_STATUS, STATUS_ERROR));
            }

            if(actionSide == ActionSide.Consumer && disputeUpdateItemData.getStatus() == DisputeItemStatus.CanceledByConsumer) {
                disputeData.getDisputeItem(disputeItemData.getId()).setStatus(disputeUpdateItemData.getStatus());
            }

            if(actionSide == ActionSide.Consumer && disputeUpdateItemData.getReason() != null) {
                disputeData.getDisputeItem(disputeItemData.getId()).setReason(disputeUpdateItemData.getReason());
            }

            if(actionSide == ActionSide.Merchant && (disputeUpdateItemData.getStatus() == DisputeItemStatus.AcceptedByMerchant ||
                    disputeUpdateItemData.getStatus() == DisputeItemStatus.RejectedByMerchant)) {
                disputeItemData.setStatus(disputeUpdateItemData.getStatus());
            }

            disputeService.update(disputeData);
        }

        return ResponseEntity.status(HttpStatus.OK).body(new Response(SUCCESS, STATUS_SUCCESS));
    }

    public ResponseEntity vote(VoteRequest request) {

        DisputeItemVoteData disputeItemVoteData = request.getDisputeItemVoteData();
        ItemVoteCrypto itemVoteCrypto = new ItemVoteCrypto();
        itemVoteCrypto.signMessage(disputeItemVoteData);

        if ( !itemVoteCrypto.verifySignature(disputeItemVoteData) ) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(UNAUTHORIZED, STATUS_ERROR));
        }

        DisputeData disputeData = disputes.getByHash(disputeItemVoteData.getDisputeHash());

        if (disputeData == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(DISPUTE_NOT_FOUND, STATUS_ERROR));
        }

        if( ! disputeData.getArbitratorHashes().contains(disputeItemVoteData.getUserHash()) ) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(UNAUTHORIZED, STATUS_ERROR));
        }

        if (disputeData.getDisputeStatus() != DisputeStatus.Claim) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(DISPUTE_NOT_IN_CLAIM_STATUS, STATUS_ERROR));
        }

        DisputeItemData disputeItemData = disputeData.getDisputeItem(disputeItemVoteData.getItemId());

        if (disputeItemData == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(ITEM_NOT_FOUND, STATUS_ERROR));
        }

        if (disputeItemData.getStatus() != DisputeItemStatus.RejectedByMerchant) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(ITEM_NOT_REJECTED_BY_MERCHANT, STATUS_ERROR));
        }

        if(disputeItemVoteData.getStatus() != DisputeItemStatus.AcceptedByArbitrators && disputeItemVoteData.getStatus() != DisputeItemStatus.RejectedByArbitrators) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(STATUS_NOT_VALID, STATUS_ERROR));
        }

        if(disputeItemData.arbitratorAlreadyVoted(disputeItemVoteData.getUserHash())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(ALREADY_GOT_YOUR_VOTE, STATUS_ERROR));
        }

        disputeItemData.addItemVoteData(disputeItemVoteData);

        disputeService.updateAfterVote(disputeData);
        disputes.put(disputeData);

        return ResponseEntity.status(HttpStatus.OK).body(new Response(SUCCESS, STATUS_SUCCESS));
    }
}
