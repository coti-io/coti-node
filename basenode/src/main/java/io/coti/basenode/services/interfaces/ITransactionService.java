package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.TransactionData;

public interface ITransactionService {

    void init();

    void handlePropagatedTransaction(TransactionData transactionData);

    int totalPostponedTransactions();

    void addToExplorerIndexes(TransactionData transactionData);
}
