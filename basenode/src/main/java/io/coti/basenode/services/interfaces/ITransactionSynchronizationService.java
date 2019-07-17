package io.coti.basenode.services.interfaces;

public interface ITransactionSynchronizationService {

    void requestMissingTransactions(long firstMissingTransactionIndex);
}
