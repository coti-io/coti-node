package io.coti.basenode.services;

import io.coti.basenode.data.FullNodeFeeData;
import io.coti.basenode.data.interfaces.IBaseTransactionData;
import io.coti.basenode.services.interfaces.IOutputBaseTransactionValidation;

public enum OutputBaseTransactionTypeValidation implements IOutputBaseTransactionValidation {
    FullNodeFee(FullNodeFeeData.class);
    private Class<? extends IBaseTransactionData> baseTransactionClass;

    <T extends IBaseTransactionData> OutputBaseTransactionTypeValidation(Class<T> baseTransactionClass) {
        this.baseTransactionClass = baseTransactionClass;
    }

    @Override
    public Class<? extends IBaseTransactionData> getBaseTransactionClass() {
        return baseTransactionClass;
    }
}