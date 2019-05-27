package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.DspConsensusResult;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TccInfo;
import io.coti.basenode.data.TransactionData;

import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

public interface IConfirmationService {
    void init();

    void setLastDspConfirmationIndex(AtomicLong maxTransactionIndex);

    void insertSavedTransaction(TransactionData transactionData, AtomicLong maxTransactionIndex);

    void insertMissingTransaction(TransactionData transactionData);

    void insertMissingConfirmation(TransactionData transactionData, Set<Hash> trustChainUnconfirmedExistingTransactionHashes);

    void setTccToTrue(TccInfo tccInfo);

    void setDspcToTrue(DspConsensusResult dspConsensusResult);

    long getTotalConfirmed();

    long getTrustChainConfirmed();

    long getDspConfirmed();

    void shutdown();
}
