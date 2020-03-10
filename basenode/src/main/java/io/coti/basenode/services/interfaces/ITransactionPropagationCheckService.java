package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionDspVote;
import io.coti.basenode.data.UnconfirmedReceivedTransactionHashData;

public interface ITransactionPropagationCheckService {

    void init();

    void updateRecoveredUnconfirmedReceivedTransactions();

    void addNewUnconfirmedTransaction(Hash transactionHash);

    void addPropagatedUnconfirmedTransaction(Hash transactionHash);

    void addUnconfirmedTransactionDSPVote(TransactionDspVote transactionDspVote);

    void putNewUnconfirmedTransaction(UnconfirmedReceivedTransactionHashData unconfirmedReceivedTransactionHashData);

    void removeTransactionHashFromUnconfirmed(Hash transactionHash);

    void removeConfirmedReceiptTransactionDSPVote(Hash transactionHash);

    void removeTransactionHashFromUnconfirmedOnBackPropagation(Hash transactionHash);

}
