package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.TransactionData;

public interface ITransactionTypeValidation {
    boolean validateBaseTransactions(TransactionData transactionData);

    boolean validateInputBaseTransactions(TransactionData transactionData);

    boolean validateOutputBaseTransactions(TransactionData transactionData);
}
