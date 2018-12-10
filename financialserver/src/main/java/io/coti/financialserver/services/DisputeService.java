package io.coti.financialserver.services;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.Response;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.financialserver.crypto.DisputeCrypto;
import io.coti.financialserver.data.MerchantDisputesData;
import io.coti.financialserver.data.ConsumerDisputesData;
import io.coti.financialserver.data.DisputeData;
import io.coti.financialserver.data.DisputeItemData;
import io.coti.financialserver.http.*;
import io.coti.financialserver.model.ConsumerDisputes;
import io.coti.financialserver.model.MerchantDisputes;
import io.coti.financialserver.model.Disputes;
import io.coti.financialserver.model.ReceiverBaseTransactionOwners;
import io.coti.financialserver.data.ReceiverBaseTransactionOwnerData;
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
public class DisputeService {

    @Autowired
    private DisputeCrypto disputeCrypto;
    @Autowired
    Disputes disputes;
    @Autowired
    ConsumerDisputes consumerDisputes;
    @Autowired
    MerchantDisputes merchantDisputes;
    @Autowired
    ReceiverBaseTransactionOwners receiverBaseTransactionOwners;

    public ResponseEntity<IResponse> createDispute(DisputeRequest disputeRequest) {

        Hash merchantHash;
        DisputeData disputeData = disputeRequest.getDisputeData();

        if (!disputeCrypto.verifySignature(disputeData)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(INVALID_SIGNATURE, STATUS_ERROR));
        }

        merchantHash = getMerchantHash(disputeData.getReceiverBaseTransactionHash());
        if(merchantHash == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(TRANSACTION_NOT_FOUND, STATUS_ERROR));
        }

        disputeData.setConsumerHash(disputeData.getUserHash());
        disputeData.setMerchantHash(merchantHash);
        disputeData.init();

        if( !isDisputeItemsValid(disputeData.getConsumerHash(), disputeData.getDisputeItems()) ) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(DISPUTE_ITEMS_EXIST_ALREADY, STATUS_ERROR));
        }

        ConsumerDisputesData consumerDisputesData = consumerDisputes.getByHash(disputeData.getUserHash());
        if(consumerDisputesData == null) {
            consumerDisputesData = new ConsumerDisputesData();
            consumerDisputesData.setHash(disputeData.getConsumerHash());
        }

        consumerDisputesData.appendDisputeHash(disputeData.getHash());
        consumerDisputes.put(consumerDisputesData);

        MerchantDisputesData merchantDisputesData = merchantDisputes.getByHash(merchantHash);
        if(merchantDisputesData == null) {
            merchantDisputesData = new MerchantDisputesData();
            merchantDisputesData.setHash(disputeData.getMerchantHash());
        }

        merchantDisputesData.appendDisputeHash(disputeData.getHash());
        merchantDisputes.put(merchantDisputesData);

        disputes.put(disputeData);

        return ResponseEntity.status(HttpStatus.OK).body(new NewDisputeResponse(disputeData.getHash().toString(), STATUS_SUCCESS));
    }

    public ResponseEntity<IResponse> getDisputeHashesOpenedByMe(DisputeRequest disputeRequest) {

        List<Hash> disputeHashes;

        if (!disputeCrypto.verifySignature(disputeRequest.getDisputeData())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(INVALID_SIGNATURE, STATUS_ERROR));
        }

        if(disputeRequest.getDisputeData().getUserHash() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(NO_CONSUMER_HASH, STATUS_ERROR));
        }

        ConsumerDisputesData consumerDisputesData = consumerDisputes.getByHash(disputeRequest.getDisputeData().getUserHash());
        disputeHashes = consumerDisputesData != null ? consumerDisputesData.getDisputeHashes() : new ArrayList<>();

        return ResponseEntity.status(HttpStatus.OK).body(new GetDisputeHashesResponse(disputeHashes));
    }

    public ResponseEntity<IResponse> getDisputeHashesOpenedOnMe(DisputeRequest disputeRequest) {

        List<Hash> disputeHashes;

        if (!disputeCrypto.verifySignature(disputeRequest.getDisputeData())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(INVALID_SIGNATURE, STATUS_ERROR));
        }

        if(disputeRequest.getDisputeData().getUserHash() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(NO_MERCHANT_HASH, STATUS_ERROR));
        }

        MerchantDisputesData merchantDisputesData = merchantDisputes.getByHash(disputeRequest.getDisputeData().getUserHash());
        disputeHashes = merchantDisputesData != null ? merchantDisputesData.getDisputeHashes() : new ArrayList<>();

        return ResponseEntity.status(HttpStatus.OK).body(new GetDisputeHashesResponse(disputeHashes));
    }

    public ResponseEntity<IResponse> getDispute(DisputeRequest disputeRequest) {

        if (!disputeCrypto.verifySignature(disputeRequest.getDisputeData())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(INVALID_SIGNATURE, STATUS_ERROR));
        }

        Hash disputeHash = disputeRequest.getDisputeData().getHash();
        DisputeData disputeData = disputes.getByHash(disputeHash);

        if (disputeData == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(DISPUTE_NOT_FOUND, STATUS_ERROR));
        }

        Hash userHash = disputeRequest.getDisputeData().getUserHash();
        if ( !disputeData.getConsumerHash().equals(userHash) && !disputeData.getMerchantHash().equals(userHash) ) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(DISPUTE_NOT_YOURS, STATUS_ERROR));
        }

        return ResponseEntity.status(HttpStatus.OK).body(new GetDisputeResponse(disputeData));
    }

    private Hash getMerchantHash(Hash transactionHash) {

        ReceiverBaseTransactionOwnerData receiverBaseTransactionOwnerData = receiverBaseTransactionOwners.getByHash(transactionHash);

        if(receiverBaseTransactionOwnerData != null) {
            return receiverBaseTransactionOwnerData.getMerchantHash();
        }

        return null;
    }

    private Boolean isDisputeItemsValid(Hash consumerHash, List<DisputeItemData> items) {

        DisputeData disputeData;
        ConsumerDisputesData consumerDisputesData = consumerDisputes.getByHash(consumerHash);

        if(consumerDisputesData == null || consumerDisputesData.getDisputeHashes() == null) {
            return true;
        }

        for(Hash disputeHash: consumerDisputesData.getDisputeHashes()){
            disputeData = disputes.getByHash(disputeHash);

            for(DisputeItemData item: items) {
                if(disputeData.getDisputeItem(item.getId()) != null) {
                    return false;
                }
            }
        }

        return true;
    }
}
