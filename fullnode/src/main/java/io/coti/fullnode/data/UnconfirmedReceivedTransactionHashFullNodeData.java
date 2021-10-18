package io.coti.fullnode.data;

import io.coti.basenode.data.UnconfirmedReceivedTransactionHashData;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UnconfirmedReceivedTransactionHashFullNodeData extends UnconfirmedReceivedTransactionHashData {

    private static final long serialVersionUID = -7338388170651037788L;
    private int retries;

    public UnconfirmedReceivedTransactionHashFullNodeData(UnconfirmedReceivedTransactionHashData unconfirmedReceivedTransactionHashData, int retries) {
        super(unconfirmedReceivedTransactionHashData);
        this.retries = retries;
    }
}
