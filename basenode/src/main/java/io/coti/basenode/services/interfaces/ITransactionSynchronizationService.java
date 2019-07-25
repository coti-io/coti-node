package io.coti.basenode.services.interfaces;

public interface ITransactionSynchronizationService {

    void requestMissingTransactionsWithWebClient(long firstMissingTransactionIndex);

    void requestMissingTransactions(long firstMissingTransactionIndex);
}
