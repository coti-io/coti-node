package io.coti.financialserver.data;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.IEntity;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import io.coti.financialserver.data.interfaces.IDisputeEvent;
import lombok.Data;
import org.apache.commons.lang3.ArrayUtils;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
public class DisputeData implements IEntity, ISignable, ISignValidatable, IDisputeEvent {

    private static final long serialVersionUID = 3311867693372600791L;
    private Hash hash;
    @NotNull
    private Hash transactionHash;
    private Instant transactionCreationTime;
    @NotNull
    private Hash consumerHash;
    private SignatureData consumerSignature;
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
    private Instant creationTime;
    private Instant updateTime;
    private Instant arbitratorsAssignTime;
    private Instant closedTime;

    private DisputeData() {

    }

    public void init() {

        disputeStatus = DisputeStatus.Recall;
        creationTime = Instant.now();
        updateTime = Instant.now();
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
    public void setSignature(SignatureData signature) {
        this.consumerSignature = signature;
    }

    @Override
    public Hash getSignerHash() {
        return consumerHash;
    }

    @Override
    public void setSignerHash(Hash hash) {
        consumerHash = hash;
    }
}
