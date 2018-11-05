package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.BaseTransactionData;

public interface IOutputBaseTransactionValidation {
    Class<? extends BaseTransactionData> getBaseTransactionClass();
}
