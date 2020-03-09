package io.coti.dspnode.data;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.UnconfirmedReceivedTransactionHashData;
import io.coti.basenode.data.interfaces.IEntity;
import lombok.Data;

@Data
public class UnconfirmedReceivedTransactionHashDspData extends UnconfirmedReceivedTransactionHashData implements IEntity {

    private static final long serialVersionUID = -3173653431370228166L;
    private boolean dSPVoteOnly;

    public UnconfirmedReceivedTransactionHashDspData(Hash transactionHash, int retries, boolean dSPVoteOnly) {
        super(transactionHash, retries);
        this.dSPVoteOnly = dSPVoteOnly;
    }
}
