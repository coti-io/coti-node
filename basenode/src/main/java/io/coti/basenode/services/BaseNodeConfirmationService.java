package io.coti.basenode.services;

import io.coti.basenode.communication.interfaces.ISender;
import io.coti.basenode.constants.BaseNodeMessages;
import io.coti.basenode.data.*;
import io.coti.basenode.model.TransactionIndexes;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.interfaces.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
public class BaseNodeConfirmationService implements IConfirmationService {

    @Autowired
    private IBalanceService balanceService;
    @Autowired
    private ICurrencyService currencyService;
    @Autowired
    private IMintingService mintingService;
    @Autowired
    private ITransactionHelper transactionHelper;
    @Autowired
    private TransactionIndexService transactionIndexService;
    @Autowired
    private TransactionIndexes transactionIndexes;
    @Autowired
    private Transactions transactions;
    @Autowired
    private IEventService eventService;
    @Autowired
    private INetworkService networkService;
    @Autowired
    private ISender sender;
    private PriorityBlockingQueue<DspConsensusResult> dcrConfirmationQueue;
    private PriorityBlockingQueue<TccInfo> tccConfirmationQueue;
    private PriorityBlockingQueue<DspConsensusResult> waitingDspConsensusResults;
    private final Map<Long, TransactionData> waitingMissingTransactionIndexes = new ConcurrentHashMap<>();
    private final AtomicLong totalConfirmed = new AtomicLong(0);
    private final AtomicLong trustChainConfirmed = new AtomicLong(0);
    private final AtomicLong dspConfirmed = new AtomicLong(0);
    private Thread dcrMessageHandlerThread;
    private Thread tccInfoMessageHandlerThread;
    private Thread missingIndexesHandler;
    private final Object initialTccConfirmationLock = new Object();
    private final Object missingIndexesLock = new Object();
    private final AtomicBoolean initialConfirmationStarted = new AtomicBoolean(false);
    private final AtomicBoolean initialTccConfirmationFinished = new AtomicBoolean(false);
    private int resendDcrCounter;

    public void init() {
        dcrConfirmationQueue = new PriorityBlockingQueue<>(11, Comparator.comparingLong(DspConsensusResult::getIndex));
        tccConfirmationQueue = new PriorityBlockingQueue<>(11, Comparator.comparing(TccInfo::getTrustChainConsensusTime));
        waitingDspConsensusResults = new PriorityBlockingQueue<>(11, Comparator.comparingLong(DspConsensusResult::getIndex));
        resendDcrCounter = 0;
        missingIndexesHandler = new Thread(this::handleMissingIndexes, "Missing Indexes");
        missingIndexesHandler.start();
        dcrMessageHandlerThread = new Thread(this::updateConfirmedDcrMessages, "Confirmation DCR");
        dcrMessageHandlerThread.start();
        tccInfoMessageHandlerThread = new Thread(this::updateConfirmedTccInfoMessages, "Confirmation TCC");
        tccInfoMessageHandlerThread.start();
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
                    reducedExistingTransactionData.getAddressAmounts().forEach(reducedExistingBaseTransactionData ->
                            balanceService.updateBalance(reducedExistingBaseTransactionData.getAddressHash(), reducedExistingBaseTransactionData.getCurrencyHash(), reducedExistingBaseTransactionData.getAmount())
                    );
                }
                transactionIndexData = nextTransactionIndexData;
            }
        } finally {
            transactionIndexService.setLastTransactionIndexData(transactionIndexData);
            log.info("Finished to set last dsp confirmation index: {}", transactionIndexData.getIndex());
        }
    }

    private void updateConfirmedDcrMessages() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                DspConsensusResult dspConsensusResult = dcrConfirmationQueue.take();
                handleDspConsensusResultUpdate(dspConsensusResult);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                log.error(BaseNodeMessages.EXCEPTION, e);
            }
        }
        LinkedList<DspConsensusResult> remainingConfirmedTransactions = new LinkedList<>();
        dcrConfirmationQueue.drainTo(remainingConfirmedTransactions);
        if (!remainingConfirmedTransactions.isEmpty()) {
            log.info("Please wait to process {} remaining DSP Consensus Results messages.", remainingConfirmedTransactions.size());
            remainingConfirmedTransactions.forEach(this::handleDspConsensusResultUpdate);
        }
    }

    private void updateConfirmedTccInfoMessages() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                TccInfo tccInfo = tccConfirmationQueue.take();
                handleTccInfoUpdate(tccInfo);
                if (initialConfirmationStarted.get() && tccConfirmationQueue.isEmpty() && !initialTccConfirmationFinished.get()) {
                    synchronized (initialTccConfirmationLock) {
                        initialTccConfirmationFinished.set(true);
                        initialTccConfirmationLock.notifyAll();
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                log.error(BaseNodeMessages.EXCEPTION, e);
            }
        }
        LinkedList<TccInfo> remainingConfirmedTransactions = new LinkedList<>();
        tccConfirmationQueue.drainTo(remainingConfirmedTransactions);
        if (!remainingConfirmedTransactions.isEmpty()) {
            log.info("Please wait to process {} remaining TCC Info messages.", remainingConfirmedTransactions.size());
            remainingConfirmedTransactions.forEach(this::handleTccInfoUpdate);
        }
    }

    private void handleTccInfoUpdate(TccInfo tccInfo) {
        transactions.lockAndGetByHash(tccInfo.getHash(), transactionData -> {
            transactionData.setTrustChainConsensus(true);
            transactionData.setTrustChainConsensusTime(tccInfo.getTrustChainConsensusTime());
            transactionData.setTrustChainTrustScore(tccInfo.getTrustChainTrustScore());
            trustChainConfirmed.incrementAndGet();
            if (transactionHelper.isConfirmed(transactionData)) {
                processConfirmedTransaction(transactionData);
            }
            transactions.put(transactionData);
        });
    }

    private void handleDspConsensusResultUpdate(DspConsensusResult dspConsensusResult) {
        transactions.lockAndGetByHash(dspConsensusResult.getHash(), transactionData -> {
            transactionData.setDspConsensusResult(dspConsensusResult);
            if (!insertNewTransactionIndex(transactionData)) {
                return;
            }
            if (transactionHelper.isDspConfirmed(transactionData)) {
                continueHandleDSPConfirmedTransaction(transactionData);
                dspConfirmed.incrementAndGet();
            }
            if (transactionHelper.isConfirmed(transactionData)) {
                processConfirmedTransaction(transactionData);
            }
            transactions.put(transactionData);
        });
    }

    protected boolean insertNewTransactionIndex(TransactionData transactionData) {
        Optional<Boolean> optionalInsertNewTransactionIndex = transactionIndexService.insertNewTransactionIndex(transactionData);
        if (!optionalInsertNewTransactionIndex.isPresent()) {
            return false;
        }
        Boolean isNewTransactionIndexInserted = optionalInsertNewTransactionIndex.get();
        DspConsensusResult dspConsensusResult = transactionData.getDspConsensusResult();
        if (Boolean.FALSE.equals(isNewTransactionIndexInserted)) {
            waitingDspConsensusResults.put(dspConsensusResult);
            synchronized (missingIndexesLock) {
                missingIndexesLock.notifyAll();
            }
            return false;
        } else {
            DspConsensusResult waitingDspConsensusResult = waitingDspConsensusResults.peek();
            if (waitingDspConsensusResult != null) {
                if (waitingDspConsensusResult.getIndex() == dspConsensusResult.getIndex() + 1) {
                    setDspcToTrue(waitingDspConsensusResults.poll());
                } else {
                    synchronized (missingIndexesLock) {
                        missingIndexesLock.notifyAll();
                    }
                }
            }
            return true;
        }
    }

    private void handleMissingIndexes() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                synchronized (missingIndexesLock) {
                    missingIndexesLock.wait(1000);
                }
                DspConsensusResult firstWaitingDcr = waitingDspConsensusResults.peek();
                while (firstWaitingDcr != null && !waitingDspConsensusResults.isEmpty()) {
                    waitForResend(firstWaitingDcr);
                    firstWaitingDcr = monitorRangeAndRequestMissingIndex(firstWaitingDcr);
                }
            } catch (InterruptedException e) {
                log.error("BaseNodeConfirmationService::handleMissingIndexes - Was Interrupted");
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                log.error(BaseNodeMessages.EXCEPTION, e);
            }
        }
    }

    private DspConsensusResult monitorRangeAndRequestMissingIndex(DspConsensusResult firstWaitingDcr) throws InterruptedException {
        DspConsensusResult dspConsensusResult = waitingDspConsensusResults.peek();
        if (dspConsensusResult != null && dspConsensusResult.getIndex() == firstWaitingDcr.getIndex()) {
            long firstMissedIndex = transactionIndexService.getLastTransactionIndexData().getIndex() + 1;
            long inRangeLastMissedIndex = firstWaitingDcr.getIndex() - 1;
            if (firstMissedIndex <= inRangeLastMissedIndex) {
                NodeResendDcrData nodeResendDcrData = new NodeResendDcrData(networkService.getNetworkNodeData().getNodeHash(),
                        networkService.getNetworkNodeData().getNodeType(), firstMissedIndex, inRangeLastMissedIndex);
                log.info("Sending Resend DCR first index: {} last index: {}, to: {}",
                        firstMissedIndex, inRangeLastMissedIndex, networkService.getRecoveryServer().getReceivingFullAddress());
                sender.send(nodeResendDcrData, networkService.getRecoveryServer().getReceivingFullAddress());
                resendDcrCounter++;
            }
        } else {
            firstWaitingDcr = dspConsensusResult;
            resendDcrCounter = 0;
        }
        return firstWaitingDcr;
    }

    private void waitForResend(DspConsensusResult firstWaitingDcr) throws InterruptedException {
        long randomWaitingTimeAcrossNodes = (long) (Math.random() * 5000) + 3000;
        Instant waitingInitialTime = Instant.now();
        boolean doneWaiting = false;
        long timeSpentWaiting = 0;
        long lastKnownIndex = transactionIndexService.getLastTransactionIndexData().getIndex();
        DspConsensusResult dspConsensusResult = waitingDspConsensusResults.peek();
        while (!doneWaiting && dspConsensusResult != null &&
                firstWaitingDcr.getIndex() == dspConsensusResult.getIndex()) {
            if (lastKnownIndex == transactionIndexService.getLastTransactionIndexData().getIndex()) {
                synchronized (missingIndexesLock) {
                    missingIndexesLock.wait(randomWaitingTimeAcrossNodes - timeSpentWaiting);
                }
                timeSpentWaiting = Instant.now().toEpochMilli() - waitingInitialTime.toEpochMilli();
                doneWaiting = randomWaitingTimeAcrossNodes <= timeSpentWaiting;
            } else {
                return;
            }
            dspConsensusResult = waitingDspConsensusResults.peek();
        }
    }

    private void processConfirmedTransaction(TransactionData transactionData) {
        Instant trustChainConsensusTime = transactionData.getTrustChainConsensusTime();
        Instant dspConsensusTime = transactionData.getDspConsensusResult().getIndexingTime();
        Instant transactionConsensusUpdateTime = trustChainConsensusTime.isAfter(dspConsensusTime) ? trustChainConsensusTime : dspConsensusTime;
        transactionData.setTransactionConsensusUpdateTime(transactionConsensusUpdateTime);
        transactionData.getBaseTransactions().forEach(baseTransactionData -> balanceService.updateBalance(baseTransactionData.getAddressHash(), baseTransactionData.getCurrencyHash(), baseTransactionData.getAmount()));
        totalConfirmed.incrementAndGet();

        transactionData.getBaseTransactions().forEach(baseTransactionData -> {
            Hash addressHash = baseTransactionData.getAddressHash();
            Hash currencyHash = baseTransactionData.getCurrencyHash();
            balanceService.continueHandleBalanceChanges(addressHash, currencyHash);
        });

        if (transactionData.getType().equals(TransactionType.TokenGeneration)) {
            currencyService.addConfirmedCurrency(transactionData);
        }

        if (transactionData.getType().equals(TransactionType.TokenMinting)) {
            mintingService.doTokenMinting(transactionData);
            continueHandleTokenChanges(transactionData);
        }

        continueHandleAddressHistoryChanges(transactionData);
        continueHandleConfirmedTransaction(transactionData);
    }

    protected void continueHandleDSPConfirmedTransaction(TransactionData transactionData) {
        if (eventService.eventHappened(Event.TRUST_SCORE_CONSENSUS) && !transactionData.isTrustChainConsensus()) {
            transactionHelper.updateTransactionOnCluster(transactionData);
        }
    }

    protected void continueHandleAddressHistoryChanges(TransactionData transactionData) {
        // implemented by the sub classes
    }

    protected void continueHandleConfirmedTransaction(TransactionData transactionData) {
        // implemented by the sub classes
    }

    @Override
    public void insertSavedTransaction(TransactionData transactionData, Map<Long, ReducedExistingTransactionData> indexToTransactionMap) {
        boolean isDspConfirmed = transactionHelper.isDspConfirmed(transactionData);
        transactionData.getBaseTransactions().forEach(baseTransactionData ->
                balanceService.updatePreBalance(baseTransactionData.getAddressHash(), baseTransactionData.getCurrencyHash(), baseTransactionData.getAmount())
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
        transactionData.getBaseTransactions().forEach(baseTransactionData -> balanceService.updatePreBalance(baseTransactionData.getAddressHash(), baseTransactionData.getCurrencyHash(), baseTransactionData.getAmount()));
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
        Optional<Boolean> optionalInsertNewTransactionIndex = transactionIndexService.insertNewTransactionIndex(transactionData);
        if (!optionalInsertNewTransactionIndex.isPresent()) {
            return;
        }
        Boolean isNewTransactionIndexInserted = optionalInsertNewTransactionIndex.get();
        DspConsensusResult dspConsensusResult = transactionData.getDspConsensusResult();
        if (Boolean.FALSE.equals(isNewTransactionIndexInserted)) {
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
            transactionData.getBaseTransactions().forEach(baseTransactionData -> balanceService.updateBalance(baseTransactionData.getAddressHash(), baseTransactionData.getCurrencyHash(), baseTransactionData.getAmount()));
            totalConfirmed.incrementAndGet();
            continueHandleConfirmedTransaction(transactionData);
        }
    }

    @Override
    public void setTccToTrue(TccInfo tccInfo) {
        tccConfirmationQueue.put(tccInfo);
    }

    @Override
    public void setDspcToTrue(DspConsensusResult dspConsensusResult) {
        dcrConfirmationQueue.put(dspConsensusResult);
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
        missingIndexesHandler.interrupt();
        dcrMessageHandlerThread.interrupt();
        tccInfoMessageHandlerThread.interrupt();
        try {
            missingIndexesHandler.join();
            dcrMessageHandlerThread.join();
            tccInfoMessageHandlerThread.join();
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
    public int getTccConfirmationQueueSize() {
        return tccConfirmationQueue.size();
    }

    @Override
    public int getDcrConfirmationQueueSize() {
        return dcrConfirmationQueue.size();
    }

    @Override
    public Object getInitialTccConfirmationLock() {
        return initialTccConfirmationLock;
    }

    @Override
    public AtomicBoolean getInitialConfirmationStarted() {
        return initialConfirmationStarted;
    }

    @Override
    public AtomicBoolean getInitialTccConfirmationFinished() {
        return initialTccConfirmationFinished;
    }

    protected void continueHandleTokenChanges(TransactionData transactionData) {
        // implemented by the sub classes
    }

    @Override
    public int getResendDcrCounter() {
        return resendDcrCounter;
    }
}
