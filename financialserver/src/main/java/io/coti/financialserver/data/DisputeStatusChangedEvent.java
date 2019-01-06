package io.coti.financialserver.data;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.interfaces.IEntity;
import io.coti.financialserver.data.interfaces.IDisputeEvent;
import lombok.Data;

@Data
public class DisputeStatusChangedEvent implements IDisputeEvent, IEntity {

    private Hash disputeHash;
    private DisputeStatus disputeStatus;

    public DisputeStatusChangedEvent(Hash disputeHash, DisputeStatus disputeStatus) {
        this.disputeHash = disputeHash;
        this.disputeStatus = disputeStatus;
    }

    @Override
    public Hash getHash() {
        return disputeHash;
    }

    @Override
    public void setHash(Hash hash) {
        disputeHash = hash;
    }
}
