package io.coti.fullnode.data;

import io.coti.basenode.data.UnconfirmedReceivedTransactionHashData;
import lombok.Data;

@Data
public class UnconfirmedReceivedTransactionHashFullNodeData extends UnconfirmedReceivedTransactionHashData {

    private int retries;

    public UnconfirmedReceivedTransactionHashFullNodeData(UnconfirmedReceivedTransactionHashData unconfirmedReceivedTransactionHashData, int retries) {
        super(unconfirmedReceivedTransactionHashData);
        this.retries = retries;
    }
}
