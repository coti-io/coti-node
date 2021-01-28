package io.coti.dspnode.data;

import io.coti.basenode.data.UnconfirmedReceivedTransactionHashData;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UnconfirmedReceivedTransactionHashDspNodeData extends UnconfirmedReceivedTransactionHashData {

    private static final long serialVersionUID = -3273912860056004989L;
    private int retries;
    private boolean dspVoteOnly;

    public UnconfirmedReceivedTransactionHashDspNodeData(UnconfirmedReceivedTransactionHashData unconfirmedReceivedTransactionHashData, int retries, boolean dspVoteOnly) {
        super(unconfirmedReceivedTransactionHashData);
        this.retries = retries;
        this.dspVoteOnly = dspVoteOnly;
    }

}
