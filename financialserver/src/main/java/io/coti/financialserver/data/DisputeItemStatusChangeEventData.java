package io.coti.financialserver.data;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.interfaces.IEntity;
import io.coti.financialserver.data.interfaces.IDisputeEvent;
import lombok.Data;

import java.util.List;

@Data
public class DisputeItemStatusChangeEventData implements IDisputeEvent, IEntity {

    private Hash disputeHash;
    private Long itemId;
    private DisputeItemStatus disputeItemStatus;

    public DisputeItemStatusChangeEventData(Hash disputeHash, Long itemId, DisputeItemStatus disputeItemStatus) {
        this.disputeHash = disputeHash;
        this.itemId = itemId;
        this.disputeItemStatus = disputeItemStatus;
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
