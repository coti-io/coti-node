package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.data.TransactionDspVote;

public interface ITransactionPropagationCheckService {

    void init();

    void updateRecoveredUnconfirmedReceivedTransactions();

    void addUnconfirmedTransaction(Hash transactionHash, boolean dSPVoteOnly);

    void addUnconfirmedTransactionDSPVote(TransactionDspVote transactionDspVote);

    void removeTransactionHashFromUnconfirmed(Hash transactionHash);

    void removeConfirmedReceiptTransactionDSPVote(Hash transactionHash);

    void removeTransactionHashFromUnconfirmedOnBackPropagation(Hash transactionHash);

    void sendUnconfirmedReceivedTransactions(long period);

    void sendUnconfirmedReceivedTransactions(TransactionData transactionData, boolean dSPVoteOnly);
}
