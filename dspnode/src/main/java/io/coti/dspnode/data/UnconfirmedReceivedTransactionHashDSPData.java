package io.coti.dspnode.data;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.UnconfirmedReceivedTransactionHashData;
import lombok.Data;

@Data
public class UnconfirmedReceivedTransactionHashDSPData extends UnconfirmedReceivedTransactionHashData {

    private int retries;
    private boolean dSPVoteOnly;

    public UnconfirmedReceivedTransactionHashDSPData(Hash transactionHash, int retries, boolean dSPVoteOnly) {
        super(transactionHash);
        this.retries = retries;
        this.dSPVoteOnly = dSPVoteOnly;
    }

    public UnconfirmedReceivedTransactionHashDSPData(UnconfirmedReceivedTransactionHashData unconfirmedReceivedTransactionHashData, int retries) {
        super(unconfirmedReceivedTransactionHashData);
        this.retries = retries;
        this.dSPVoteOnly = false;
    }

}
