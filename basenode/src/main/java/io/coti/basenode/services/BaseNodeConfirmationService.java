package io.coti.basenode.services;

import io.coti.basenode.data.*;
import io.coti.basenode.http.data.TransactionStatus;
import io.coti.basenode.model.TransactionIndexes;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.interfaces.IBalanceService;
import io.coti.basenode.services.interfaces.IConfirmationService;
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
    private BlockingQueue<ConfirmationData> confirmationQueue;
    private Map<Long, DspConsensusResult> waitingDspConsensusResults = new ConcurrentHashMap<>();
    private Map<Long, TransactionData> waitingMissingTransactionIndexes = new ConcurrentHashMap<>();
    private AtomicLong totalConfirmed = new AtomicLong(0);
    private AtomicLong trustChainConfirmed = new AtomicLong(0);
    private AtomicLong dspConfirmed = new AtomicLong(0);
    private Thread confirmedTransactionsThread;

    public void init() {
        confirmationQueue = new LinkedBlockingQueue<>();
        confirmedTransactionsThread = new Thread(this::updateConfirmedTransactions);
        confirmedTransactionsThread.start();
        log.info("{} is up", this.getClass().getSimpleName());
    }

    @Override
    public void setLastDspConfirmationIndex(AtomicLong maxTransactionIndex) {
        log.info("Started to set last dsp confirmation index");
        byte[] accumulatedHash = "GENESIS".getBytes();
        TransactionIndexData transactionIndexData = new TransactionIndexData(new Hash(-1), -1, "GENESIS".getBytes());
        TransactionIndexData nextTransactionIndexData;
        try {
            for (long i = 0; i <= maxTransactionIndex.get(); i++) {
                nextTransactionIndexData = transactionIndexes.getByHash(new Hash(i));
                if (nextTransactionIndexData == null) {
                    log.error("Null transaction index data found for index {}", i);
                    return;
                }

                TransactionData transactionData = transactions.getByHash(nextTransactionIndexData.getTransactionHash());
                if (transactionData == null) {
                    log.error("Null transaction data found for index {}", i);
                    return;
                }
                if (transactionData.getDspConsensusResult() == null) {
                    log.error("Null dsp consensus result found for index {} and transaction {}", i, transactionData.getHash());
                    return;
                }
                accumulatedHash = transactionIndexService.getAccumulatedHash(accumulatedHash, transactionData.getHash(), transactionData.getDspConsensusResult().getIndex());
                if (!Arrays.equals(accumulatedHash, nextTransactionIndexData.getAccumulatedHash())) {
                    log.error("Incorrect accumulated hash");
                    return;
                }
                dspConfirmed.incrementAndGet();
                if (transactionHelper.isConfirmed(transactionData)) {
                    totalConfirmed.incrementAndGet();
                    balanceService.commitBaseTransactions(transactionData);
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
                updateConfirmedTransactionHandler(confirmationData);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
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
            if (transactionHelper.isTccConfirmedDspRejected(transactionData)) {
                processDSPRejectedTransaction(transactionData);
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
                setDspcToTrueOrFalse(waitingDspConsensusResults.get(index));
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
        totalConfirmed.incrementAndGet();
        balanceService.commitBaseTransactions(transactionData);
        continueHandleAddressHistoryChanges(transactionData, TransactionStatus.CONFIRMED);
    }

    private void processDSPRejectedTransaction(TransactionData transactionData) {
        Instant trustChainConsensusTime = transactionData.getTrustChainConsensusTime();
        Instant dspConsensusTime = transactionData.getDspConsensusResult().getIndexingTime();
        Instant transactionConsensusUpdateTime = trustChainConsensusTime.isAfter(dspConsensusTime) ? trustChainConsensusTime : dspConsensusTime;
        transactionData.setTransactionConsensusUpdateTime(transactionConsensusUpdateTime);
        balanceService.rollbackBaseTransactions(transactionData);
        continueHandleAddressHistoryChanges(transactionData, TransactionStatus.REJECTED);
    }

    protected void continueHandleDSPConfirmedTransaction(TransactionData transactionData) {
        // implemented by the sub classes
    }

    protected void continueHandleAddressHistoryChanges(TransactionData transactionData, TransactionStatus transactionStatus) {
        // implemented for full node
    }

    @Override
    public void insertSavedTransaction(TransactionData transactionData, AtomicLong maxTransactionIndex) {
        if (transactionData.getType() != TransactionType.ZeroSpend && !transactionHelper.isTccConfirmedDspRejected(transactionData)) {
            transactionData.getBaseTransactions().forEach(baseTransactionData ->
                    balanceService.updatePreBalance(baseTransactionData.getAddressHash(), baseTransactionData.getAmount())
            );
        }
        if (!transactionHelper.hasDspVotingAndIndexed(transactionData)) {
            transactionHelper.addNoneIndexedTransaction(transactionData);
        }
        if (transactionData.getDspConsensusResult() != null) {
            maxTransactionIndex.set(Math.max(maxTransactionIndex.get(), transactionData.getDspConsensusResult().getIndex()));
        }

        if (transactionData.isTrustChainConsensus()) {
            trustChainConfirmed.incrementAndGet();
        }

    }

    @Override
    public void insertMissingTransaction(TransactionData transactionData) {
        if (transactionData.getType() != TransactionType.ZeroSpend) {
            transactionData.getBaseTransactions().forEach(baseTransactionData ->
                    balanceService.updatePreBalance(baseTransactionData.getAddressHash(), baseTransactionData.getAmount()));
        }
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
        if (!transactionHelper.hasDspVotingAndIndexed(transactionData)) {
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
            totalConfirmed.incrementAndGet();
            balanceService.commitBaseTransactions(transactionData);
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
    public void setDspcToTrueOrFalse(DspConsensusResult dspConsensusResult) {
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

}
