package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.*;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public interface IConfirmationService {

    void init();

    void setLastDspConfirmationIndex(Map<Long, ReducedExistingTransactionData> indexToTransactionMap);

    void insertSavedTransaction(TransactionData transactionData, Map<Long, ReducedExistingTransactionData> indexToTransactionMap);

    void insertMissingTransaction(TransactionData transactionData);

    void insertMissingConfirmation(TransactionData transactionData, Set<Hash> trustChainUnconfirmedExistingTransactionHashes);

    void setTccToTrue(TccInfo tccInfo);

    void setDspcToTrue(DspConsensusResult dspConsensusResult);

    long getTotalConfirmed();

    long getTrustChainConfirmed();

    long getDspConfirmed();

    void shutdown();

    int getWaitingDspConsensusResultsMapSize();

    int getWaitingMissingTransactionIndexesSize();

    int getTccConfirmationQueueSize();

    int getDcrConfirmationQueueSize();

    Object getInitialTccConfirmationLock();

    AtomicBoolean getInitialConfirmationStarted();

    AtomicBoolean getInitialTccConfirmationFinished();

    int getResendDcrCounter();

    void continueHandleDSPConfirmedTransaction(TransactionData transactionData);

    boolean insertNewTransactionIndex(TransactionData transactionData);
}
