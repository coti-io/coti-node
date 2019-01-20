package io.coti.financialserver.data;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.interfaces.IEntity;
import io.coti.financialserver.data.interfaces.IDisputeEvent;
import lombok.Data;

@Data
public class DisputeItemStatusChangeEventData implements IDisputeEvent, IEntity {

    private Hash disputeHash;
    private Long itemId;
    private DisputeItemStatus disputeItemStatus;
    private Hash transactionHash;

    public DisputeItemStatusChangeEventData(DisputeData disputeData, Long itemId) {
        this.disputeHash = disputeData.getHash();
        this.itemId = itemId;
        this.disputeItemStatus = disputeData.getDisputeItem(itemId).getStatus();
        this.transactionHash = disputeData.getTransactionHash();
    }

    @Override
    public Hash getHash() {
        return disputeHash;
    }

    @Override
    public void setHash(Hash hash) {
        this.disputeHash = hash;
    }
}
