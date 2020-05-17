package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.*;

import java.util.Map;
import java.util.Set;

public interface IConfirmationService {

    void init();

    void setLastDspConfirmationIndex(Map<Long, ReducedExistingTransactionData> indexToTransactionMap);

    void insertSavedTransaction(TransactionData transactionData, Map<Long, ReducedExistingTransactionData> indexToTransactionMap);

    void insertMissingTransaction(TransactionData transactionData);

    void insertMissingConfirmation(TransactionData transactionData, Set<Hash> trustChainUnconfirmedExistingTransactionHashes);

    void setTccToTrue(TccInfo tccInfo);

    void setDspc(DspConsensusResult dspConsensusResult);

    long getTotalConsensus();

    long getTrustChainConfirmed();

    long getDspConfirmed();

    long getDspRejected();

    void shutdown();
}
