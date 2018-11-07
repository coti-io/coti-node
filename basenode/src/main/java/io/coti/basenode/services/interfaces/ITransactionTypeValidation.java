package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.TransactionData;

public interface ITransactionTypeValidation {
    boolean validateOutputBaseTransactions(TransactionData transactionData);
}
