package io.coti.basenode.services;

import io.coti.basenode.data.*;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.LiveView.LiveViewService;
import io.coti.basenode.services.interfaces.IBalanceService;
import io.coti.basenode.services.interfaces.IConfirmationService;
import io.coti.basenode.services.interfaces.ITransactionHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
public class BaseNodeConfirmationService implements IConfirmationService {
    @Autowired
    private LiveViewService liveViewService;
    @Autowired
    private IBalanceService balanceService;
    @Autowired
    private ITransactionHelper transactionHelper;
    @Autowired
    private TransactionIndexService transactionIndexService;
    @Autowired
    private Transactions transactions;
    private BlockingQueue<ConfirmationData> confirmationQueue;
    private Map<Long, DspConsensusResult> waitingDspConsensusResults = new ConcurrentHashMap<>();
    private Map<Long, TransactionData> waitingMissingTransactionIndexes = new ConcurrentHashMap<>();
    private AtomicLong totalConfirmed = new AtomicLong(0);
    private AtomicLong tccConfirmed = new AtomicLong(0);
    private AtomicLong dspConfirmed = new AtomicLong(0);
    private Thread confirmedTransactionsThread;

    public void init() {
        confirmationQueue = new LinkedBlockingQueue<>();
        confirmedTransactionsThread = new Thread(() -> updateConfirmedTransactions());
        confirmedTransactionsThread.start();
        log.info("{} is up", this.getClass().getSimpleName());
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
        if (remainingConfirmedTransactions.size() != 0) {
            log.info("Please wait to process {} remaining confirmed transaction(s)", remainingConfirmedTransactions.size());
            remainingConfirmedTransactions.forEach(confirmationData -> updateConfirmedTransactionHandler(confirmationData));
        }
    }

    private void updateConfirmedTransactionHandler(ConfirmationData confirmationData) {
        TransactionData transactionData = transactions.getByHash(confirmationData.getHash());
        if (confirmationData instanceof TccInfo) {
            transactionData.setTrustChainConsensus(true);
            transactionData.setTrustChainTrustScore(((TccInfo) confirmationData).getTrustChainTrustScore());
            transactionData.setTrustChainTransactionHashes(((TccInfo) confirmationData).getTrustChainTransactionHashes());
            tccConfirmed.incrementAndGet();
        } else if (confirmationData instanceof DspConsensusResult) {
            transactionData.setDspConsensusResult((DspConsensusResult) confirmationData);
            if (!insertNewTransactionIndex(transactionData)) {
                return;
            }
            if (transactionHelper.isDspConfirmed(transactionData)) {
                dspConfirmed.incrementAndGet();
            }
        }
        if (transactionHelper.isConfirmed(transactionData)) {
            processConfirmedTransaction(transactionData);
        }
        transactions.put(transactionData);
    }

    protected boolean insertNewTransactionIndex(TransactionData transactionData) {
        DspConsensusResult dspConsensusResult = transactionData.getDspConsensusResult();
        if (dspConsensusResult == null) {
            return false;
        }
        if (!transactionIndexService.insertNewTransactionIndex(transactionData)) {
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
        transactionData.setTransactionConsensusUpdateTime(Instant.now());
        transactionData.getBaseTransactions().forEach(baseTransactionData -> balanceService.updateBalance(baseTransactionData.getAddressHash(), baseTransactionData.getAmount()));
        totalConfirmed.incrementAndGet();

        liveViewService.updateTransactionStatus(transactionData, 2);

        transactionData.getBaseTransactions().forEach(baseTransactionData -> {
            Hash addressHash = baseTransactionData.getAddressHash();
            balanceService.continueHandleBalanceChanges(addressHash);
        });

        continueHandleAddressHistoryChanges(transactionData);
    }

    protected void continueHandleAddressHistoryChanges(TransactionData transactionData) {
    }

    @Override
    public void insertSavedTransaction(TransactionData transactionData) {
        boolean isConfirmed = transactionHelper.isConfirmed(transactionData);
        boolean isDspConfirmed = transactionHelper.isDspConfirmed(transactionData);
        transactionData.getBaseTransactions().forEach(baseTransactionData -> {
            balanceService.updatePreBalance(baseTransactionData.getAddressHash(), baseTransactionData.getAmount());
            if (isConfirmed) {
                balanceService.updateBalance(baseTransactionData.getAddressHash(), baseTransactionData.getAmount());
            }
        });
        if (isDspConfirmed) {
            dspConfirmed.incrementAndGet();
        }
        if (transactionData.isTrustChainConsensus()) {
            tccConfirmed.incrementAndGet();
        }
        if (isConfirmed) {
            totalConfirmed.incrementAndGet();
        }
    }

    @Override
    public void insertMissingTransaction(TransactionData transactionData) {
        transactionData.getBaseTransactions().forEach(baseTransactionData -> balanceService.updatePreBalance(baseTransactionData.getAddressHash(), baseTransactionData.getAmount()));
        if (transactionData.isTrustChainConsensus()) {
            tccConfirmed.incrementAndGet();
        }
        if (transactionData.getDspConsensusResult() == null) {
            transactionHelper.addNoneIndexedTransaction(transactionData);
        } else {
            if (insertMissingTransactionIndex(transactionData)) {
                return;
            }
            if (transactionHelper.isDspConfirmed(transactionData)) {
                dspConfirmed.incrementAndGet();
            }
        }

    }

    private boolean insertMissingTransactionIndex(TransactionData transactionData) {
        DspConsensusResult dspConsensusResult = transactionData.getDspConsensusResult();
        if (!transactionIndexService.insertNewTransactionIndex(transactionData)) {
            waitingMissingTransactionIndexes.put(dspConsensusResult.getIndex(), transactionData);
            return false;
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
            return true;
        }
    }

    private void processMissingDspConfirmedTransaction(TransactionData transactionData) {
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
    public long getTccConfirmed() {
        return tccConfirmed.get();
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
