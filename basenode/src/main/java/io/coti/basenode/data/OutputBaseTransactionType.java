package io.coti.basenode.data;

import io.coti.basenode.data.interfaces.IBaseTransactionType;

public enum OutputBaseTransactionType implements IBaseTransactionType {
    FullNodeFee,
    NetworkFee,
    RollingReserve,
    Payment,
    Transfer;
}
