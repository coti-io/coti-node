package io.coti.financialserver.data;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.IEntity;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DisputeData implements IEntity, ISignable, ISignValidatable {
    private Hash hash;
    private Hash transactionHash;
    private Hash consumerHash;
    private Hash merchantHash;
    private List<Hash> arbitratorHashes;
    private List<DisputeItemData> disputeItems;
    private DisputeStatus disputeStatus;
    private BigDecimal amount;
    private BigDecimal chargeBackAmount;
    private Hash chargeBackTransactionHash;
    private BigDecimal recourseClaimAmount;
    private Boolean recourseClaimOpen;
    private Hash recourseClaimTransactionHash;
    private Date creationTime;
    private Date closedTime;
    private SignatureData signature;

    public DisputeData(Hash consumerHash, Hash transactionHash, List<DisputeItemData> disputeItems, BigDecimal amount) {

        this.transactionHash = transactionHash;
        this.consumerHash = consumerHash;
        this.disputeStatus = DisputeStatus.Recall;
        this.disputeItems = disputeItems;
        this.amount = amount;
        this.creationTime = new Date();

        byte[] concatDateAndUserHashBytes = ArrayUtils.addAll(consumerHash.getBytes(),creationTime.toString().getBytes());
        this.hash = CryptoHelper.cryptoHash( concatDateAndUserHashBytes );
    }

    public List<DisputeItemData> getDisputeItems() {
        return disputeItems;
    }

    public DisputeItemData getDisputeItem(Long itemId) {
        for (DisputeItemData disputeItem : disputeItems) {
            if(disputeItem.getId() == itemId) {
                return disputeItem;
            }
        }

        return null;
    }

    public Hash getTransactionHash() {
        return transactionHash;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Hash getConsumerHash() {
        return consumerHash;
    }

    public Hash getMerchantHash() {
        return merchantHash;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    @Override
    public Hash getHash() {
        return hash;
    }

    @Override
    public void setHash(Hash hash) {
        this.hash = hash;
    }

    @Override
    public SignatureData getSignature() {
        return signature;
    }

    @Override
    public Hash getSignerHash() {
        return consumerHash;
    }

    @Override
    public void setSignerHash(Hash hash) {
        consumerHash = hash;
    }

    @Override
    public void setSignature(SignatureData signature) {
        this.signature = signature;
    }
}
