package io.coti.financialserver.data;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.interfaces.IEntity;
import io.coti.financialserver.data.interfaces.IDisputeEvent;
import lombok.Data;

@Data
public class DisputeStatusChangeEventData implements IDisputeEvent, IEntity {

    private static final long serialVersionUID = 5159650195944069500L;
    private Hash disputeHash;
    private DisputeStatus disputeStatus;
    private Hash transactionHash;

    public DisputeStatusChangeEventData(DisputeData disputeData) {
        this.disputeHash = disputeData.getHash();
        this.disputeStatus = disputeData.getDisputeStatus();
        this.transactionHash = disputeData.getTransactionHash();
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
