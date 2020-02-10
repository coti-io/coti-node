package io.coti.basenode.services;

import io.coti.basenode.communication.JacksonSerializer;
import io.coti.basenode.data.DspConsensusResult;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.model.TransactionIndexes;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.interfaces.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.FluxSink;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BaseNodeTransactionService implements ITransactionService {

    @Autowired
    private ITransactionHelper transactionHelper;
    @Autowired
    private IValidationService validationService;
    @Autowired
    private IDspVoteService dspVoteService;
    @Autowired
    private IConfirmationService confirmationService;
    @Autowired
    private IClusterService clusterService;
    @Autowired
    private Transactions transactions;
    @Autowired
    private TransactionIndexService transactionIndexService;
    @Autowired
    private JacksonSerializer jacksonSerializer;
    @Autowired
    private TransactionIndexes transactionIndexes;
    private Map<Hash, TransactionData> parentProcessingTransactions = new ConcurrentHashMap<>();
    private List<TransactionData> postponedTransactions = new LinkedList<>();

    @Override
    public void init() {
        log.info("{} is up", this.getClass().getSimpleName());
    }

    @Override
    public void getTransactionBatch(long startingIndex, HttpServletResponse response) {

        AtomicLong transactionNumber = new AtomicLong(0);
        Thread monitorTransactionBatch = monitorTransactionBatch(Thread.currentThread().getId(), transactionNumber);

        try {
            ServletOutputStream output = response.getOutputStream();

            monitorTransactionBatch.start();

            if (startingIndex <= transactionIndexService.getLastTransactionIndexData().getIndex()) {
                for (long i = startingIndex; i <= transactionIndexService.getLastTransactionIndexData().getIndex(); i++) {
                    output.write(jacksonSerializer.serialize(transactions.getByHash(transactionIndexes.getByHash(new Hash(i)).getTransactionHash())));
                    output.flush();
                    transactionNumber.incrementAndGet();
                }
            }
            for (Hash hash : transactionHelper.getNoneIndexedTransactionHashes()) {
                output.write(jacksonSerializer.serialize(transactions.getByHash(hash)));
                output.flush();
                transactionNumber.incrementAndGet();

            }

        } catch (Exception e) {
            log.error("Error sending transaction batch");
            log.error(e.getMessage());
        } finally {
            if (monitorTransactionBatch.isAlive()) {
                monitorTransactionBatch.interrupt();
            }
        }
    }

    @Override
    public void getTransactionBatch(long startingIndex, FluxSink sink) {
        AtomicLong transactionNumber = new AtomicLong(0);
        Thread monitorTransactionBatch = monitorTransactionBatch(Thread.currentThread().getId(), transactionNumber);

        try {
            monitorTransactionBatch.start();

            if (startingIndex <= transactionIndexService.getLastTransactionIndexData().getIndex()) {
                for (long i = startingIndex; i <= transactionIndexService.getLastTransactionIndexData().getIndex(); i++) {
                    sink.next((jacksonSerializer.serialize(transactions.getByHash(transactionIndexes.getByHash(new Hash(i)).getTransactionHash()))));
                    transactionNumber.incrementAndGet();
                }
            }

            for (Hash hash : transactionHelper.getNoneIndexedTransactionHashes()) {
                sink.next(jacksonSerializer.serialize((transactions.getByHash(hash))));
                transactionNumber.incrementAndGet();

            }
            sink.complete();
        } catch (Exception e) {
            log.error("Error sending reactive transaction batch");
            log.error(e.getMessage());
        } finally {
            if (monitorTransactionBatch.isAlive()) {
                monitorTransactionBatch.interrupt();
            }
        }
    }

    private Thread monitorTransactionBatch(long threadId, AtomicLong transactionNumber) {
        return new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                log.info("Transaction batch: thread id = {}, transactionNumber= {}", threadId, transactionNumber);
            }
        });
    }

    @Override
    public void handlePropagatedTransaction(TransactionData transactionData) {
        if (transactionHelper.isTransactionAlreadyPropagated(transactionData)) {
            log.debug("Transaction already exists: {}", transactionData.getHash());
            return;
        }
        List<Hash> childrenTransactions = transactionData.getChildrenTransactionHashes();
        try {
            transactionHelper.startHandleTransaction(transactionData);
            while (hasOneOfParentsProcessing(transactionData)) {
                parentProcessingTransactions.put(transactionData.getHash(), transactionData);
                synchronized (transactionData) {
                    transactionData.wait();
                }
            }
            if (!validationService.validatePropagatedTransactionDataIntegrity(transactionData)) {
                log.error("Data Integrity validation failed: {}", transactionData.getHash());
                return;
            }
            if (hasOneOfParentsMissing(transactionData)) {
                postponedTransactions.add(transactionData);
                return;
            }
            if (!validationService.validateBalancesAndAddToPreBalance(transactionData)) {
                log.error("Balance check failed: {}", transactionData.getHash());
                return;
            }
            transactionHelper.attachTransactionToCluster(transactionData);
            transactionHelper.setTransactionStateToSaved(transactionData);

            continueHandlePropagatedTransaction(transactionData);
            transactionHelper.setTransactionStateToFinished(transactionData);
        } catch (InterruptedException e) {
            log.info("Transaction thread wait interrupted");
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("Transaction propagation handler error:");
            e.printStackTrace();
        } finally {
            boolean isTransactionFinished = transactionHelper.isTransactionFinished(transactionData);
            transactionHelper.endHandleTransaction(transactionData);
            for (Hash childrenTransactionHash : childrenTransactions) {
                TransactionData childrenTransaction = parentProcessingTransactions.get(childrenTransactionHash);
                if (childrenTransaction != null)
                    synchronized (childrenTransaction) {
                        childrenTransaction.notify();
                        parentProcessingTransactions.remove(childrenTransactionHash);
                    }
            }
            if (isTransactionFinished) {
                DspConsensusResult postponedDspConsensusResult = dspVoteService.getPostponedDspConsensusResult(transactionData.getHash());
                if (postponedDspConsensusResult != null) {
                    dspVoteService.handleVoteConclusion(postponedDspConsensusResult);
                }
                List<TransactionData> postponedParentTransactions = postponedTransactions.stream().filter(
                        postponedTransactionData ->
                                (postponedTransactionData.getRightParentHash() != null && postponedTransactionData.getRightParentHash().equals(transactionData.getHash())) ||
                                        (postponedTransactionData.getLeftParentHash() != null && postponedTransactionData.getLeftParentHash().equals(transactionData.getHash())))
                        .collect(Collectors.toList());
                postponedParentTransactions.forEach(postponedTransaction -> {
                    log.debug("Handling postponed transaction : {}, parent of transaction: {}", postponedTransaction.getHash(), transactionData.getHash());
                    postponedTransactions.remove(postponedTransaction);
                    handlePropagatedTransaction(postponedTransaction);
                });
            }

        }

    }

    protected void continueHandlePropagatedTransaction(TransactionData transactionData) {
        // implemented by sub classes
    }

    public void handleMissingTransaction(TransactionData transactionData, Set<Hash> trustChainUnconfirmedExistingTransactionHashes) {

        if (!transactionHelper.isTransactionExists(transactionData)) {

            transactions.put(transactionData);
            addToExplorerIndexes(transactionData);
            transactionHelper.incrementTotalTransactions();

            confirmationService.insertMissingTransaction(transactionData);
            propagateMissingTransaction(transactionData);

        } else {
            transactions.put(transactionData);
            confirmationService.insertMissingConfirmation(transactionData, trustChainUnconfirmedExistingTransactionHashes);
        }
        clusterService.addMissingTransactionOnInit(transactionData, trustChainUnconfirmedExistingTransactionHashes);

    }

    protected void propagateMissingTransaction(TransactionData transactionData) {
        log.debug("Propagate missing transaction {} by base node", transactionData.getHash());
    }

    public Thread monitorTransactionThread(String type, AtomicLong transactionNumber, AtomicLong receivedTransactionNumber) {
        return new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(5000);

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                if (receivedTransactionNumber != null) {
                    log.info("Received {} transactions: {}, inserted transactions: {}", type, receivedTransactionNumber, transactionNumber);
                } else {
                    log.info("Inserted {} transactions: {}", type, transactionNumber);
                }
            }
        });
    }

    public void addToExplorerIndexes(TransactionData transactionData) {
        log.debug("Adding the transaction {} to explorer indexes by base node", transactionData.getHash());
    }

    private boolean hasOneOfParentsProcessing(TransactionData transactionData) {
        return (transactionData.getLeftParentHash() != null && transactionHelper.isTransactionHashProcessing(transactionData.getLeftParentHash())) ||
                (transactionData.getRightParentHash() != null && transactionHelper.isTransactionHashProcessing(transactionData.getRightParentHash()));
    }

    private boolean hasOneOfParentsMissing(TransactionData transactionData) {
        return (transactionData.getLeftParentHash() != null && transactions.getByHash(transactionData.getLeftParentHash()) == null) ||
                (transactionData.getRightParentHash() != null && transactions.getByHash(transactionData.getRightParentHash()) == null);
    }

    public int totalPostponedTransactions() {
        return postponedTransactions.size();
    }
}
