package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.interfaces.IBaseTransactionData;

public interface IOutputBaseTransactionValidation {
    Class<? extends IBaseTransactionData> getBaseTransactionClass();
}
