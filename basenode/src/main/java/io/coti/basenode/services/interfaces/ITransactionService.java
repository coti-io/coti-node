package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.TransactionData;

import java.util.concurrent.atomic.AtomicLong;

public interface ITransactionService {

    void init();

    void handlePropagatedTransaction(TransactionData transactionData);

    int totalPostponedTransactions();

    long incrementAndGetExplorerIndex();
}
