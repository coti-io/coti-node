package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionDspVote;

public interface ITransactionPropagationCheckService {

    void init();

    void updateRecoveredUnconfirmedReceivedTransactions();

    void addUnconfirmedTransactionDSPVote(TransactionDspVote transactionDspVote);

    void removeTransactionHashFromUnconfirmed(Hash transactionHash);

    void removeConfirmedReceiptTransactionDSPVote(Hash transactionHash);

    void removeTransactionHashFromUnconfirmedOnBackPropagation(Hash transactionHash);

    void sendUnconfirmedReceivedTransactions(long period);
}
