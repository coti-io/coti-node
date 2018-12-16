package io.coti.financialserver.services;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.http.Response;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.model.Transactions;
import io.coti.financialserver.crypto.DisputeCrypto;
import io.coti.financialserver.crypto.GetDisputeCrypto;
import io.coti.financialserver.data.*;
import io.coti.financialserver.http.GetDisputesRequest;
import io.coti.financialserver.http.GetDisputesResponse;
import io.coti.financialserver.http.NewDisputeRequest;
import io.coti.financialserver.http.NewDisputeResponse;
import io.coti.financialserver.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static io.coti.financialserver.http.HttpStringConstants.*;

@Slf4j
@Service
public class DisputeService {

    private static final int COUNT_ARBITRATORS_PER_DISPUTE = 2;

    @Value("#{'${arbitrators.userHashes}'.split(',')}")
    private List<String> ARBITRATOR_USER_HASHES;

    @Autowired
    private GetDisputeCrypto getDisputeCrypto;
    @Autowired
    private DisputeCrypto disputeCrypto;
    @Autowired
    private Transactions transactions;
    @Autowired
    private Disputes disputes;
    @Autowired
    private ConsumerDisputes consumerDisputes;
    @Autowired
    private MerchantDisputes merchantDisputes;
    @Autowired
    private ArbitratorDisputes arbitratorDisputes;
    @Autowired
    private ReceiverBaseTransactionOwners receiverBaseTransactionOwners;

    public ResponseEntity<IResponse> createDispute(NewDisputeRequest newDisputeRequest) {

        DisputeData disputeData = newDisputeRequest.getDisputeData();

        disputeCrypto.signMessage(disputeData);
        if (!disputeCrypto.verifySignature(disputeData)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(INVALID_SIGNATURE, STATUS_ERROR));
        }

        TransactionData transactionData = transactions.getByHash(disputeData.getTransactionHash());

        if (transactionData == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(DISPUTE_TRANSACTION_NOT_FOUND, STATUS_ERROR));
        }

        if (!disputeData.getConsumerHash().equals(transactionData.getSenderHash())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(DISPUTE_CONSUMER_INVALID, STATUS_ERROR));
        }

        Hash merchantHash = getMerchantHash(transactionData.getReceiverBaseTransactionHash());
        if (merchantHash == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(DISPUTE_MERCHANT_NOT_FOUND, STATUS_ERROR));
        }

        disputeData.setMerchantHash(merchantHash);
        disputeData.init();

        if (!isDisputeItemsValid(disputeData.getConsumerHash(), disputeData.getDisputeItems(), disputeData.getTransactionHash())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(DISPUTE_ITEMS_EXIST_ALREADY, STATUS_ERROR));
        }

        UserDisputesData consumerDisputesData = consumerDisputes.getByHash(disputeData.getConsumerHash());
        if (consumerDisputesData == null) {
            consumerDisputesData = new UserDisputesData();
            consumerDisputesData.setHash(disputeData.getConsumerHash());
        }

        UserDisputesData merchantDisputesData = merchantDisputes.getByHash(merchantHash);
        if (merchantDisputesData == null) {
            merchantDisputesData = new UserDisputesData();
            merchantDisputesData.setHash(merchantHash);
        }

        consumerDisputesData.appendDisputeHash(disputeData.getHash());
        merchantDisputesData.appendDisputeHash(disputeData.getHash());
        consumerDisputes.put(consumerDisputesData);
        merchantDisputes.put(merchantDisputesData);
        disputes.put(disputeData);

        return ResponseEntity.status(HttpStatus.OK).body(new NewDisputeResponse(disputeData.getHash().toString(), STATUS_SUCCESS));
    }

    public ResponseEntity<IResponse> getDisputes(GetDisputesRequest getDisputesRequest) {

        GetDisputeData getDisputesData = getDisputesRequest.getGetDisputeData();

        getDisputeCrypto.signMessage(getDisputesData);
        if (!getDisputeCrypto.verifySignature(getDisputesData)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(INVALID_SIGNATURE, STATUS_ERROR));
        }

        List<DisputeData> disputesData = new ArrayList<>();

        UserDisputesData userDisputesData;
        if(getDisputesData.getDisputeSide() == ActionSide.Consumer) {
            userDisputesData = consumerDisputes.getByHash(getDisputesRequest.getGetDisputeData().getUserHash());
        }
        else if(getDisputesData.getDisputeSide() == ActionSide.Merchant) {
            userDisputesData = merchantDisputes.getByHash(getDisputesRequest.getGetDisputeData().getUserHash());
        }
        else if(getDisputesData.getDisputeSide() == ActionSide.Arbitrator) {
            userDisputesData = arbitratorDisputes.getByHash(getDisputesRequest.getGetDisputeData().getUserHash());
        }
        else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(DISPUTE_SIDE_NOT_CORRECT, STATUS_ERROR));
        }

        List<Hash> userDisputeHashes = userDisputesData.getDisputeHashes();

        if(getDisputesData.getDisputeHashes() == null) {
            getDisputesData.setDisputeHashes(userDisputesData.getDisputeHashes());
        }

        for(Hash disputeHash : getDisputesData.getDisputeHashes()) {
            DisputeData disputeData = disputes.getByHash(disputeHash);

            if (disputeData == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(disputeHash + DISPUTE_NOT_FOUND, STATUS_ERROR));
            }

            if(!userDisputeHashes.contains(disputeHash)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(DISPUTE_NOT_YOURS, STATUS_ERROR));
            }

            disputesData.add(disputeData);
        }

        return ResponseEntity.status(HttpStatus.OK).body(new GetDisputesResponse(disputesData));
    }

    public void update(DisputeData dispute) {
        dispute.setUpdateTime(new Date());

        boolean noRecallItems = true;
        boolean allItemsAcceptedByMerchant = true;
        boolean allItemsCancelledByConsumer = true;
        boolean atLeastOneItemRejectedByMerchant = false;

        for (DisputeItemData disputeItem : dispute.getDisputeItems()) {

            if (disputeItem.getStatus() == DisputeItemStatus.Recall) {
                noRecallItems = false;
                allItemsAcceptedByMerchant = false;
                allItemsCancelledByConsumer = false;
                break;
            }

            if (disputeItem.getStatus() == DisputeItemStatus.AcceptedByMerchant) {
                allItemsCancelledByConsumer = false;
            }

            if (disputeItem.getStatus() == DisputeItemStatus.CanceledByConsumer) {
                allItemsAcceptedByMerchant = false;
            }

            if (disputeItem.getStatus() == DisputeItemStatus.RejectedByMerchant) {
                allItemsAcceptedByMerchant = false;
                allItemsCancelledByConsumer = false;
                atLeastOneItemRejectedByMerchant = true;
            }
        }

        if (noRecallItems && atLeastOneItemRejectedByMerchant) {
            dispute.setDisputeStatus(DisputeStatus.Claim);
            assignToArbitrators(dispute);
        } else if (allItemsAcceptedByMerchant) {
            dispute.setDisputeStatus(DisputeStatus.AcceptedByMerchant);
        } else if (allItemsCancelledByConsumer) {
            dispute.setDisputeStatus(DisputeStatus.CanceledByConsumer);
        } else if (noRecallItems) {
            dispute.setDisputeStatus(DisputeStatus.AcceptedByMerchantCanceledByConsumer);
        }

        disputes.put(dispute);
    }

    public void updateAfterVote(DisputeData dispute) {
        dispute.setUpdateTime(new Date());
    }

    private void assignToArbitrators(DisputeData dispute) {

        int random;
        for (int i = 0; i < COUNT_ARBITRATORS_PER_DISPUTE; i++) {

            random = (int) ((Math.random() * ARBITRATOR_USER_HASHES.size()));
            dispute.getArbitratorHashes().add(new Hash(ARBITRATOR_USER_HASHES.get(random)));
            ARBITRATOR_USER_HASHES.remove(random);
        }
    }

    private Hash getMerchantHash(Hash receiverBaseTransactionHash) {
        ReceiverBaseTransactionOwnerData receiverBaseTransactionOwnerData = receiverBaseTransactionOwners.getByHash(receiverBaseTransactionHash);

        if (receiverBaseTransactionOwnerData != null) {
            return receiverBaseTransactionOwnerData.getMerchantHash();
        }

        return null;
    }

    private Boolean isDisputeItemsValid(Hash consumerHash, List<DisputeItemData> items, Hash transactionHash) {

        DisputeData disputeData;
        UserDisputesData userDisputesData = consumerDisputes.getByHash(consumerHash);

        if (userDisputesData == null || userDisputesData.getDisputeHashes() == null) {
            return true;
        }

        for (Hash disputeHash : userDisputesData.getDisputeHashes()) {
            disputeData = disputes.getByHash(disputeHash);

            for (DisputeItemData item : items) {
                if (disputeData.getDisputeItem(item.getId()) != null && disputeData.getTransactionHash().equals(transactionHash)) {
                    return false;
                }
            }
        }

        return true;
    }
}
