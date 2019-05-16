package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.TransactionData;

import javax.servlet.http.HttpServletResponse;

public interface ITransactionService {

    void init();

    void getTransactionBatch(long startingIndex, HttpServletResponse response);

    void handlePropagatedTransaction(TransactionData transactionData);

    int totalPostponedTransactions();

    void addToExplorerIndexes(TransactionData transactionData);
}
