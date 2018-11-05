package io.coti.basenode.data;

import io.coti.basenode.data.interfaces.IBaseTransactionType;

public enum InputBaseTransactionType implements IBaseTransactionType {
    Payment,
    Transfer;
}
