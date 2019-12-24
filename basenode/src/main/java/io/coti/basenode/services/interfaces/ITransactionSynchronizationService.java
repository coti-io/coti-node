package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;

public interface ITransactionSynchronizationService {

    void requestMissingTransactions(long firstMissingTransactionIndex);

    TransactionData requestSingleMissingTransaction(Hash transactionHash);

    TransactionData requestSingleMissingTransactionFromRecovery(Hash transactionHash);
}
