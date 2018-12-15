package io.coti.financialserver.data;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.IEntity;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import lombok.Data;
import org.apache.commons.lang3.ArrayUtils;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class DisputeData implements IEntity, ISignable, ISignValidatable {

    private Hash hash;
    @NotNull
    private Hash transactionHash;
    @NotNull
    private Hash consumerHash;
    @NotNull
    private @Valid SignatureData consumerSignature;
    private Hash merchantHash;
    private List<Hash> arbitratorHashes;
    private List<@Valid DisputeItemData> disputeItems;
    private DisputeStatus disputeStatus;
    private BigDecimal amount;
    private BigDecimal chargeBackAmount;
    private Hash chargeBackTransactionHash;
    private BigDecimal recourseClaimAmount;
    private Boolean recourseClaimOpen;
    private Hash recourseClaimTransactionHash;
    private Date creationTime;
    private Date updateTime;
    private Date closedTime;

    private DisputeData() {

    }

    public void init() {

        disputeStatus = DisputeStatus.Recall;
        creationTime = new Date();
        updateTime = new Date();
        arbitratorHashes = new ArrayList<>();

        byte[] concatDateAndUserHashBytes = ArrayUtils.addAll(consumerHash.getBytes(), creationTime.toString().getBytes());
        hash = CryptoHelper.cryptoHash(concatDateAndUserHashBytes);
    }

    public List<DisputeItemData> getDisputeItems() {
        return disputeItems;
    }

    public DisputeItemData getDisputeItem(Long itemId) {
        for (DisputeItemData disputeItem : disputeItems) {
            if (disputeItem.getId().equals(itemId)) {
                return disputeItem;
            }
        }

        return null;
    }

    public List<DisputeItemData> getDisputeItems(List<Long> itemIds) {
        List<DisputeItemData> disputeItems = new ArrayList<>();
        for (DisputeItemData disputeItem : this.disputeItems) {
            if (itemIds.contains(disputeItem.getId())) {
                disputeItems.add(disputeItem);
            }
        }

        return disputeItems;
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
        return consumerSignature;
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
        this.consumerSignature = signature;
    }
}
