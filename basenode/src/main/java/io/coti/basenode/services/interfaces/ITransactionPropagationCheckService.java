package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionDspVote;
import io.coti.basenode.data.UnconfirmedReceivedTransactionHashData;

import java.util.List;

public interface ITransactionPropagationCheckService {

    void init();

    void recoverUnconfirmedReceivedTransactions();

    default void addNewUnconfirmedTransaction(Hash transactionHash) {
    }

    default void removeConfirmedReceivedTransactions(List<Hash> confirmedReceiptTransactions) {
    }

    default void putNewUnconfirmedTransaction(UnconfirmedReceivedTransactionHashData unconfirmedReceivedTransactionHashData) {
    }

    void removeTransactionHashFromUnconfirmed(Hash transactionHash);

    default void removeConfirmedReceiptTransaction(Hash transactionHash) {
    }

    void addUnconfirmedTransactionDSPVote(TransactionDspVote transactionDspVote);

    void addPropagatedUnconfirmedTransaction(Hash hash);
}
