package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;

public interface ITransactionPropagationCheckService {

    void init();

    void updateRecoveredUnconfirmedReceivedTransactions();

    void addUnconfirmedTransaction(Hash transactionHash);

    void removeTransactionHashFromUnconfirmed(Hash transactionHash);

    void removeTransactionHashFromUnconfirmedOnBackPropagation(Hash transactionHash);

    void sendUnconfirmedReceivedTransactions(long period);

    void sendUnconfirmedReceivedTransactions(TransactionData transactionData);

}
