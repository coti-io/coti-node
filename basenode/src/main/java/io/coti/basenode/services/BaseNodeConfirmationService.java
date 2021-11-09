package io.coti.basenode.services;

import io.coti.basenode.data.*;
import io.coti.basenode.model.TransactionIndexes;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.interfaces.IBalanceService;
import io.coti.basenode.services.interfaces.IClusterService;
import io.coti.basenode.services.interfaces.IConfirmationService;
import io.coti.basenode.services.interfaces.IDAGLockHelper;
import io.coti.basenode.services.interfaces.ITransactionHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
public class BaseNodeConfirmationService implements IConfirmationService {

    @Autowired
    private IBalanceService balanceService;
    @Autowired
    private ITransactionHelper transactionHelper;
    @Autowired
    private TransactionIndexService transactionIndexService;
    @Autowired
    private TransactionIndexes transactionIndexes;
    @Autowired
    private Transactions transactions;
    @Autowired
    private TrustChainConfirmationService trustChainConfirmationService;
    @Autowired
    private IClusterService clusterService;
    @Autowired
    private IDAGLockHelper dagLockHelper;
    private BlockingQueue<ConfirmationData> confirmationQueue;
    private final Map<Long, DspConsensusResult> waitingDspConsensusResults = new ConcurrentHashMap<>();
    private final Map<Long, TransactionData> waitingMissingTransactionIndexes = new ConcurrentHashMap<>();
    private final AtomicLong totalConfirmed = new AtomicLong(0);
    private final AtomicLong trustChainConfirmed = new AtomicLong(0);
    private final AtomicLong dspConfirmed = new AtomicLong(0);
    private Thread confirmedTransactionsThread;

    public void init() {
        confirmationQueue = new LinkedBlockingQueue<>();
        confirmedTransactionsThread = new Thread(this::updateConfirmedTransactions);
        confirmedTransactionsThread.start();
        log.info("{} is up", this.getClass().getSimpleName());
    }

    @Override
    public void setLastDspConfirmationIndex(Map<Long, ReducedExistingTransactionData> indexToTransactionMap) {
        log.info("Started to set last dsp confirmation index");
        byte[] accumulatedHash = "GENESIS".getBytes();
        TransactionIndexData transactionIndexData = new TransactionIndexData(new Hash(-1), -1, "GENESIS".getBytes());
        TransactionIndexData nextTransactionIndexData;
        try {
            for (long i = 0; i < indexToTransactionMap.size(); i++) {
                nextTransactionIndexData = transactionIndexes.getByHash(new Hash(i));
                if (nextTransactionIndexData == null) {
                    log.error("Null transaction index data found for index {}", i);
                    return;
                }

                ReducedExistingTransactionData reducedExistingTransactionData = indexToTransactionMap.get(i);
                if (reducedExistingTransactionData == null) {
                    log.error("Null transaction data found for index {}", i);
                    return;
                }
                accumulatedHash = transactionIndexService.getAccumulatedHash(accumulatedHash, reducedExistingTransactionData.getHash(), i);
                if (!Arrays.equals(accumulatedHash, nextTransactionIndexData.getAccumulatedHash())) {
                    log.error("Incorrect accumulated hash");
                    return;
                }
                dspConfirmed.incrementAndGet();
                if (reducedExistingTransactionData.isTrustChainConsensus()) {
                    totalConfirmed.incrementAndGet();
                    reducedExistingTransactionData.getAddressAmounts().forEach(addressAmount ->
                            balanceService.updateBalance(addressAmount.getKey(), addressAmount.getValue())
                    );
                }
                transactionIndexData = nextTransactionIndexData;
            }
        } finally {
            transactionIndexService.setLastTransactionIndexData(transactionIndexData);
            log.info("Finished to set last dsp confirmation index: {}", transactionIndexData.getIndex());
        }
    }

    private void updateConfirmedTransactions() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                ConfirmationData confirmationData = confirmationQueue.take();
                dagLockHelper.lockForRead();
                updateConfirmedTransactionHandler(confirmationData);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                dagLockHelper.unlockForRead();
            }
        }
        LinkedList<ConfirmationData> remainingConfirmedTransactions = new LinkedList<>();
        confirmationQueue.drainTo(remainingConfirmedTransactions);
        if (!remainingConfirmedTransactions.isEmpty()) {
            log.info("Please wait to process {} remaining confirmed transaction(s)", remainingConfirmedTransactions.size());
            remainingConfirmedTransactions.forEach(this::updateConfirmedTransactionHandler);
        }
    }

    private void updateConfirmedTransactionHandler(ConfirmationData confirmationData) {
        transactions.lockAndGetByHash(confirmationData.getHash(), transactionData -> {
            if (confirmationData instanceof TccInfo) {
                transactionData.setTrustChainConsensus(true);
                transactionData.setTrustChainConsensusTime(((TccInfo) confirmationData).getTrustChainConsensusTime());
                transactionData.setTrustChainTrustScore(((TccInfo) confirmationData).getTrustChainTrustScore());
                trustChainConfirmed.incrementAndGet();
            } else if (confirmationData instanceof DspConsensusResult) {
                transactionData.setDspConsensusResult((DspConsensusResult) confirmationData);
                if (!insertNewTransactionIndex(transactionData)) {
                    return;
                }
                if (transactionHelper.isDspConfirmed(transactionData)) {
                    continueHandleDSPConfirmedTransaction(transactionData);
                    dspConfirmed.incrementAndGet();
                }
            }
            if (transactionHelper.isConfirmed(transactionData)) {
                processConfirmedTransaction(transactionData);
            }
            transactions.put(transactionData);
        });

    }

    protected boolean insertNewTransactionIndex(TransactionData transactionData) {
        Boolean insertNewTransactionIndex = transactionIndexService.insertNewTransactionIndex(transactionData);
        if (insertNewTransactionIndex == null) {
            return false;
        }
        DspConsensusResult dspConsensusResult = transactionData.getDspConsensusResult();
        if (!insertNewTransactionIndex) {
            waitingDspConsensusResults.put(dspConsensusResult.getIndex(), dspConsensusResult);
            return false;
        } else {
            long index = dspConsensusResult.getIndex() + 1;
            while (waitingDspConsensusResults.containsKey(index)) {
                setDspcToTrue(waitingDspConsensusResults.get(index));
                waitingDspConsensusResults.remove(index);
                index++;
            }
            return true;
        }
    }

    private void processConfirmedTransaction(TransactionData transactionData) {
        Instant trustChainConsensusTime = transactionData.getTrustChainConsensusTime();
        Instant dspConsensusTime = transactionData.getDspConsensusResult().getIndexingTime();
        Instant transactionConsensusUpdateTime = trustChainConsensusTime.isAfter(dspConsensusTime) ? trustChainConsensusTime : dspConsensusTime;
        transactionData.setTransactionConsensusUpdateTime(transactionConsensusUpdateTime);
        transactionData.getBaseTransactions().forEach(baseTransactionData -> balanceService.updateBalance(baseTransactionData.getAddressHash(), baseTransactionData.getAmount()));
        totalConfirmed.incrementAndGet();

        transactionData.getBaseTransactions().forEach(baseTransactionData -> {
            Hash addressHash = baseTransactionData.getAddressHash();
            balanceService.continueHandleBalanceChanges(addressHash);
        });

        continueHandleAddressHistoryChanges(transactionData);
    }

    protected void continueHandleDSPConfirmedTransaction(TransactionData transactionData) {
        // implemented by the sub classes
    }

    protected void continueHandleAddressHistoryChanges(TransactionData transactionData) {
        // implemented by the sub classes
    }

    protected void continueHandleAddressHistoryRollbackChanges(TransactionData transactionData) {
        // implemented by the sub classes
    }

    @Override
    public void insertSavedTransaction(TransactionData transactionData, Map<Long, ReducedExistingTransactionData> indexToTransactionMap) {
        boolean isDspConfirmed = transactionHelper.isDspConfirmed(transactionData);
        transactionData.getBaseTransactions().forEach(baseTransactionData ->
                balanceService.updatePreBalance(baseTransactionData.getAddressHash(), baseTransactionData.getAmount())
        );
        if (!isDspConfirmed) {
            transactionHelper.addNoneIndexedTransaction(transactionData);
        }
        if (transactionData.getDspConsensusResult() != null) {
            indexToTransactionMap.put(transactionData.getDspConsensusResult().getIndex(), new ReducedExistingTransactionData(transactionData));
        }

        if (transactionData.isTrustChainConsensus()) {
            trustChainConfirmed.incrementAndGet();
        }

    }

    @Override
    public void insertMissingTransaction(TransactionData transactionData) {
        transactionData.getBaseTransactions().forEach(baseTransactionData -> balanceService.updatePreBalance(baseTransactionData.getAddressHash(), baseTransactionData.getAmount()));
        if (transactionData.isTrustChainConsensus()) {
            trustChainConfirmed.incrementAndGet();
        }
        insertMissingDspConfirmation(transactionData);
    }

    @Override
    public void insertMissingConfirmation(TransactionData transactionData, Set<Hash> trustChainUnconfirmedExistingTransactionHashes) {
        if (trustChainUnconfirmedExistingTransactionHashes.contains(transactionData.getHash()) && transactionData.isTrustChainConsensus()) {
            trustChainConfirmed.incrementAndGet();
        }
        insertMissingDspConfirmation(transactionData);
    }

    private void insertMissingDspConfirmation(TransactionData transactionData) {
        if (!transactionHelper.isDspConfirmed(transactionData)) {
            transactionHelper.addNoneIndexedTransaction(transactionData);
        }
        if (transactionData.getDspConsensusResult() != null) {
            insertMissingTransactionIndex(transactionData);
        }
    }

    private void insertMissingTransactionIndex(TransactionData transactionData) {
        Boolean insertNewTransactionIndex = transactionIndexService.insertNewTransactionIndex(transactionData);
        if (insertNewTransactionIndex == null) {
            return;
        }
        DspConsensusResult dspConsensusResult = transactionData.getDspConsensusResult();
        if (!insertNewTransactionIndex) {
            waitingMissingTransactionIndexes.put(dspConsensusResult.getIndex(), transactionData);
        } else {
            processMissingDspConfirmedTransaction(transactionData);
            long index = dspConsensusResult.getIndex() + 1;
            while (waitingMissingTransactionIndexes.containsKey(index)) {
                TransactionData waitingMissingTransactionData = waitingMissingTransactionIndexes.get(index);
                transactionIndexService.insertNewTransactionIndex(waitingMissingTransactionData);
                processMissingDspConfirmedTransaction(waitingMissingTransactionData);
                waitingMissingTransactionIndexes.remove(index);
                index++;
            }
        }
    }

    private void processMissingDspConfirmedTransaction(TransactionData transactionData) {
        continueHandleDSPConfirmedTransaction(transactionData);
        dspConfirmed.incrementAndGet();
        if (transactionData.isTrustChainConsensus()) {
            transactionData.getBaseTransactions().forEach(baseTransactionData -> balanceService.updateBalance(baseTransactionData.getAddressHash(), baseTransactionData.getAmount()));
            totalConfirmed.incrementAndGet();
        }
    }

    @Override
    public void setTccToTrue(TccInfo tccInfo) {
        try {
            confirmationQueue.put(tccInfo);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void setDspcToTrue(DspConsensusResult dspConsensusResult) {
        try {
            confirmationQueue.put(dspConsensusResult);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public long getTotalConfirmed() {
        return totalConfirmed.get();
    }

    @Override
    public long getTrustChainConfirmed() {
        return trustChainConfirmed.get();
    }

    @Override
    public long getDspConfirmed() {
        return dspConfirmed.get();
    }

    public void shutdown() {
        log.info("Shutting down {}", this.getClass().getSimpleName());
        confirmedTransactionsThread.interrupt();
        try {
            confirmedTransactionsThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Interrupted shutdown {}", this.getClass().getSimpleName());
        }

    }

    @Override
    public int getWaitingDspConsensusResultsMapSize() {
        return waitingDspConsensusResults.size();
    }

    @Override
    public int getWaitingMissingTransactionIndexesSize() {
        return waitingMissingTransactionIndexes.size();
    }

    @Override
    public int getQueueSize() {
        return confirmationQueue.size();
    }


    private void updateTransactionParentsTrustScoresRecursive(TransactionData transactionData) {
        if (transactionData.getLeftParentHash() != null) {
            restoreTransactionTrustScoreRecursive(transactionData.getLeftParentHash());
        }
        if (transactionData.getRightParentHash() != null) {
            restoreTransactionTrustScoreRecursive(transactionData.getRightParentHash());
        }
    }

    private void restoreTransactionTrustScoreRecursive(Hash transactionHash) {
        TransactionData transactionData = transactions.getByHash(transactionHash);
        if (transactionData != null) {
            int threshold = trustChainConfirmationService.getThreshold();
            boolean previousTrustChainConsensus = transactionData.isTrustChainConsensus();
            double previousTrustChainTrustScore = transactionData.getTrustChainTrustScore();
            double maxSonTotalTrustScore = trustChainConfirmationService.getMaxSonTotalTrustScore(transactionData);
            double updatedTrustScore = transactionData.getSenderTrustScore() + maxSonTotalTrustScore;
            boolean passedThreshold = updatedTrustScore >= threshold;

            if (previousTrustChainConsensus && !passedThreshold && revertTransactionTrustChainConsensus(transactionData) ) {
                log.debug("Transaction {} TCC consensus was reverted successfully ", transactionData.getHash());
            }
            transactionData.setTrustChainTrustScore(updatedTrustScore);
            log.debug("Transaction with hash:{} totalTrustScore was updated from: {} to:{} ", transactionData.getHash(), previousTrustChainTrustScore, transactionData.getTrustChainTrustScore());
            trustChainConfirmationService.updateTrustChainConfirmationCluster(transactionData);
            if (!previousTrustChainConsensus || !passedThreshold) {
                clusterService.updateTrustChainConfirmationCluster(transactionData);
            }

            if (!passedThreshold) {
                updateTransactionParentsTrustScoresRecursive(transactionData);
            }
        } else {
            throw new IllegalArgumentException(String.format("Transaction %s for trust score related revert expected parent", transactionHash));
        }
    }

    @Override
    public boolean revertTransactionTrustChainConsensus(TransactionData transactionData) {
        if (transactionData == null) {
            throw new IllegalArgumentException("Transaction to revert trust score consensus is null");
        }
        if (!transactionData.isTrustChainConsensus()) {
            log.debug("Transaction {} TCC was already false ", transactionData.getHash());
            return false;
        }
        transactionData.setTrustChainConsensus(false);
        transactionData.setTrustChainTrustScore(0);
        Instant dspConsensusTime = transactionData.getDspConsensusResult() != null ? transactionData.getDspConsensusResult().getIndexingTime() : null;
        transactionData.setTransactionConsensusUpdateTime(dspConsensusTime);
        transactionData.setTrustChainConsensusTime(null);
        trustChainConfirmed.decrementAndGet();
        log.debug("Transaction {} TCC reverted back to unconfirmed ", transactionData.getHash());

        if (transactionData.getDspConsensusResult() != null && transactionData.getDspConsensusResult().isDspConsensus()) {
            totalConfirmed.decrementAndGet();
            balanceService.rollbackBaseTransactionsBalance(transactionData);
            continueHandleAddressHistoryRollbackChanges(transactionData);
            log.debug("Transaction {} reverted to unconfirmed successfully ", transactionData.getHash());
        }
        transactions.put(transactionData);
        return true;
    }

    @Override
    public void revertTrustScoreBasedOnAlienatedChildTransaction(TransactionData transactionData) {
        if (transactionData == null) {
            throw new IllegalArgumentException("Transaction for trust score related revert passed as null");
        }
        if (transactionHelper.isAlienated(transactionData)) {
            clusterService.sortTrustChainConfirmationClusterByTopologicalOrder();
            trustChainConfirmationService.updateTrustChainClusterTransactions();
            updateTransactionParentsTrustScoresRecursive(transactionData);
        } else {
            throw new IllegalArgumentException(String.format("Transaction %s for trust score related revert expected parents alienated", transactionData.getHash()));
        }
    }

}
