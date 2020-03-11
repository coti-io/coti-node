package io.coti.dspnode.data;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.UnconfirmedReceivedTransactionHashData;
import lombok.Data;

@Data
public class UnconfirmedReceivedTransactionHashDspNodeData extends UnconfirmedReceivedTransactionHashData {

    private int retries;
    private boolean dspVoteOnly;

    public UnconfirmedReceivedTransactionHashDspNodeData(Hash transactionHash, int retries, boolean dspVoteOnly) {
        super(transactionHash);
        this.retries = retries;
        this.dspVoteOnly = dspVoteOnly;
    }

    public UnconfirmedReceivedTransactionHashDspNodeData(UnconfirmedReceivedTransactionHashData unconfirmedReceivedTransactionHashData, int retries) {
        super(unconfirmedReceivedTransactionHashData);
        this.retries = retries;
        this.dspVoteOnly = false;
    }

}
