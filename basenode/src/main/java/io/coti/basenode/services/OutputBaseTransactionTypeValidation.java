package io.coti.basenode.services;

import io.coti.basenode.data.BaseTransactionData;
import io.coti.basenode.data.FullNodeFeeData;
import io.coti.basenode.services.interfaces.IOutputBaseTransactionValidation;

public enum OutputBaseTransactionTypeValidation implements IOutputBaseTransactionValidation {
    FullNodeFee(FullNodeFeeData.class);
    private Class<? extends BaseTransactionData> baseTransactionClass;

    <T extends BaseTransactionData> OutputBaseTransactionTypeValidation(Class<T> baseTransactionClass) {
        this.baseTransactionClass = baseTransactionClass;
    }

    @Override
    public Class<? extends BaseTransactionData> getBaseTransactionClass() {
        return baseTransactionClass;
    }
}