package io.coti.financialserver.services;

import io.coti.basenode.http.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import io.coti.financialserver.data.ActionSide;
import io.coti.financialserver.data.DisputeData;
import io.coti.financialserver.data.DisputeItemStatus;
import io.coti.financialserver.crypto.ItemCrypto;
import io.coti.financialserver.data.DisputeItemData;
import io.coti.financialserver.http.ItemRequest;
import io.coti.financialserver.model.Disputes;

import java.util.List;

import static io.coti.financialserver.http.HttpStringConstants.*;

@Slf4j
@Service
public class ItemService {

    @Autowired
    Disputes disputes;

    @Autowired
    DisputeService disputeService;

    public ResponseEntity updated(ItemRequest request) {

        DisputeItemData disputeItemDataNew = request.getDisputeItemData();
        ItemCrypto itemCrypto = new ItemCrypto();
        itemCrypto.signMessage(disputeItemDataNew);

        if ( !itemCrypto.verifySignature(disputeItemDataNew) ) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(UNAUTHORIZED, STATUS_ERROR));
        }

        DisputeData disputeData = disputes.getByHash(disputeItemDataNew.getDisputeHash());

        if (disputeData == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(DISPUTE_NOT_FOUND, STATUS_ERROR));
        }

        DisputeItemData disputeItemData = disputeData.getDisputeItem(disputeItemDataNew.getId());

        if (disputeItemData == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(ITEM_NOT_FOUND, STATUS_ERROR));
        }

        if (disputeItemData.getStatus() != DisputeItemStatus.Recall) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(DISPUTE_PASSED_RECALL_STATUS, STATUS_ERROR));
        }

        ActionSide actionSide;
        if(disputeData.getConsumerHash().equals(disputeItemDataNew.getUserHash())) {
            actionSide = ActionSide.Consumer;
        }
        else if(disputeData.getMerchantHash().equals(disputeItemDataNew.getUserHash())) {
            actionSide = ActionSide.Merchant;
        }
        else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(UNAUTHORIZED, STATUS_ERROR));
        }
        // TODO: remove this line
        actionSide = ActionSide.Merchant;

        if(actionSide == ActionSide.Consumer && disputeItemDataNew.getStatus() == DisputeItemStatus.CanceledByConsumer) {
            disputeData.getDisputeItem(disputeItemDataNew.getId()).setStatus(disputeItemDataNew.getStatus());
        }

        if(actionSide == ActionSide.Consumer && disputeItemDataNew.getReason() != null) {
            disputeData.getDisputeItem(disputeItemDataNew.getId()).setReason(disputeItemDataNew.getReason());
        }

        if(actionSide == ActionSide.Merchant && (disputeItemDataNew.getStatus() == DisputeItemStatus.AcceptedByMerchant ||
                                                 disputeItemDataNew.getStatus() == DisputeItemStatus.RejectedByMerchant)) {
            disputeItemData.setStatus(disputeItemDataNew.getStatus());
        }

        disputeService.update(disputeData);

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(SUCCESS, STATUS_SUCCESS));
    }
}
