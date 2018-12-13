package io.coti.financialserver.data;

import lombok.Data;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.ArrayUtils;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.IEntity;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;

@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "type")
public class DisputeData implements IEntity, ISignable, ISignValidatable {
    private Hash hash;
    private Hash receiverBaseTransactionHash;
    private Hash userHash;
    private Hash consumerHash;
    @NotNull
    private @Valid SignatureData userSignature;
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
    private Date closedTime;

    public void init() {

        this.disputeStatus = DisputeStatus.Recall;
        this.creationTime = new Date();

        byte[] concatDateAndUserHashBytes = ArrayUtils.addAll(consumerHash.getBytes(),creationTime.toString().getBytes());
        this.hash = CryptoHelper.cryptoHash( concatDateAndUserHashBytes );
    }

    public List<DisputeItemData> getDisputeItems() {
        return disputeItems;
    }

    public DisputeItemData getDisputeItem(Long itemId) {
        for (DisputeItemData disputeItem : disputeItems) {
            if(disputeItem.getId().equals(itemId)) {
                return disputeItem;
            }
        }

        return null;
    }

    public List<DisputeItemData> getDisputeItems(List<Long> itemIds) {
        List<DisputeItemData> disputeItems = new ArrayList<>();
        for (DisputeItemData disputeItem : this.disputeItems) {
            if(itemIds.contains(disputeItem.getId())) {
                disputeItems.add(disputeItem);
            }
        }

        return disputeItems;
    }

    public void updateStatus() {

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
        return userSignature;
    }

    @Override
    public Hash getSignerHash() {
        return userHash;
    }

    @Override
    public void setSignerHash(Hash hash) {
        userHash = hash;
    }

    @Override
    public void setSignature(SignatureData signature) {
        this.userSignature = signature;
    }
}
