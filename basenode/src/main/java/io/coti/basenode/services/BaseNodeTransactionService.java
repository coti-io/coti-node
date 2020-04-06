package io.coti.basenode.services;

import io.coti.basenode.communication.JacksonSerializer;
import io.coti.basenode.data.*;
import io.coti.basenode.database.interfaces.IDatabaseConnector;
import io.coti.basenode.model.TransactionIndexes;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.interfaces.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.FluxSink;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
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
    @Autowired
    private IClusterStampService clusterStampService;
    @Autowired
    private IDatabaseConnector databaseConnector;
    @Autowired
    private INetworkService networkService;
    @Autowired
    private ICurrencyService currencyService;
    protected Map<TransactionData, Boolean> postponedTransactions = new ConcurrentHashMap<>();  // true/false means new from full node or propagated transaction
    private final LockData transactionLockData = new LockData();

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
    public void getTransactionBatch(long startingIndex, FluxSink<byte[]> sink) {
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
        AtomicBoolean isTransactionAlreadyPropagated = new AtomicBoolean(false);

        try {
            checkTransactionAlreadyPropagatedAndStartHandle(transactionData, isTransactionAlreadyPropagated);
            if (isTransactionAlreadyPropagated.get()) {
                removeTransactionHashFromUnconfirmed(transactionData);
                log.debug("Transaction already exists: {}", transactionData.getHash());
                return;
            }
            if (!validationService.validatePropagatedTransactionDataIntegrity(transactionData)) {
                log.error("Data Integrity validation failed: {}", transactionData.getHash());
                return;
            }
            if (hasOneOfParentsMissing(transactionData)) {
                if (!postponedTransactions.containsKey(transactionData)) {
                    postponedTransactions.put(transactionData, false);
                }
                return;
            }
            if (!validationService.validateBalancesAndAddToPreBalance(transactionData)) {
                log.error("Balance check failed: {}", transactionData.getHash());
                return;
            }
            if (transactionData.getType().equals(TransactionType.TokenGeneration) && !validationService.checkTokenUniqueness(transactionData)) {
                log.error("Not unique token generation attempt by transaction: {}", transactionData.getHash());
                return;
            }
            if (transactionData.getType().equals(TransactionType.TokenMinting) && !validationService.validateTokenMintingAndAddToAllocatedAmount(transactionData)) {
                log.error("Minting balance check failed: {}", transactionData.getHash());
                return;
            }
            transactionHelper.attachTransactionToCluster(transactionData);
            transactionHelper.setTransactionStateToSaved(transactionData);

            continueHandlePropagatedTransaction(transactionData);
            transactionHelper.setTransactionStateToFinished(transactionData);
        } catch (Exception e) {
            log.error("Transaction propagation handler error:", e);
        } finally {
            if (!isTransactionAlreadyPropagated.get()) {
                boolean isTransactionFinished = transactionHelper.isTransactionFinished(transactionData);
                transactionHelper.endHandleTransaction(transactionData);
                if (isTransactionFinished) {
                    processPostponedTransactions(transactionData);
                }
            }
        }
    }

    protected void checkTransactionAlreadyPropagatedAndStartHandle(TransactionData transactionData, AtomicBoolean isTransactionAlreadyPropagated) {
        try {
            synchronized (transactionLockData.addLockToLockMap(transactionData.getHash())) {
                isTransactionAlreadyPropagated.set(transactionHelper.isTransactionAlreadyPropagated(transactionData));
                if (!isTransactionAlreadyPropagated.get()) {
                    transactionHelper.startHandleTransaction(transactionData);
                }
            }
        } finally {
            transactionLockData.removeLockFromLocksMap(transactionData.getHash());
        }
    }

    public void removeTransactionHashFromUnconfirmed(TransactionData transactionData) {
        // Implemented by Full Node
    }

    protected void processPostponedTransactions(TransactionData transactionData) {
        DspConsensusResult postponedDspConsensusResult = dspVoteService.getPostponedDspConsensusResult(transactionData.getHash());
        if (postponedDspConsensusResult != null) {
            dspVoteService.handleVoteConclusion(postponedDspConsensusResult);
        }
        Map<TransactionData, Boolean> postponedParentTransactions = postponedTransactions.entrySet().stream().filter(
                postponedTransactionMapEntry ->
                        (postponedTransactionMapEntry.getKey().getRightParentHash() != null
                                && postponedTransactionMapEntry.getKey().getRightParentHash().equals(transactionData.getHash()))
                                || (postponedTransactionMapEntry.getKey().getLeftParentHash() != null
                                && postponedTransactionMapEntry.getKey().getLeftParentHash().equals(transactionData.getHash())))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        postponedParentTransactions.forEach((postponedTransaction, isTransactionFromFullNode) -> {
            log.debug("Handling postponed transaction : {}, parent of transaction: {}", postponedTransaction.getHash(), transactionData.getHash());
            postponedTransactions.remove(postponedTransaction);
            handlePostponedTransaction(postponedTransaction, isTransactionFromFullNode);
        });
    }

    protected void handlePostponedTransaction(TransactionData postponedTransaction, boolean isTransactionFromFullNode) {
        if (!isTransactionFromFullNode) {
            handlePropagatedTransaction(postponedTransaction);
        }
    }

    protected void continueHandlePropagatedTransaction(TransactionData transactionData) {
        // implemented by sub classes
    }

    @Override
    public void handleMissingTransaction(TransactionData transactionData, Set<Hash> trustChainUnconfirmedExistingTransactionHashes, EnumMap<InitializationTransactionHandlerType, ExecutorData> missingTransactionExecutorMap) {
        boolean transactionExists = transactionHelper.isTransactionExists(transactionData);
        transactions.put(transactionData);
        if (!transactionExists) {
            missingTransactionExecutorMap.get(InitializationTransactionHandlerType.TRANSACTION).submit(() -> {
                addToExplorerIndexes(transactionData);
                propagateMissingTransaction(transactionData);
            });
            missingTransactionExecutorMap.get(InitializationTransactionHandlerType.CONFIRMATION).submit(() -> confirmationService.insertMissingTransaction(transactionData));
            transactionHelper.incrementTotalTransactions();
            currencyService.handleMissingTransaction(transactionData);
            continueHandleMissingTransaction(transactionData);
        } else {
            missingTransactionExecutorMap.get(InitializationTransactionHandlerType.CONFIRMATION).submit(() -> confirmationService.insertMissingConfirmation(transactionData, trustChainUnconfirmedExistingTransactionHashes));
        }
        missingTransactionExecutorMap.get(InitializationTransactionHandlerType.CLUSTER).submit(() -> clusterService.addMissingTransactionOnInit(transactionData, trustChainUnconfirmedExistingTransactionHashes));

    }

    protected void continueHandleMissingTransaction(TransactionData transactionData) {
        log.debug("Continue handle missing transaction {} by base node", transactionData.getHash());
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

    protected boolean hasOneOfParentsMissing(TransactionData transactionData) {
        return (transactionData.getLeftParentHash() != null && transactions.getByHash(transactionData.getLeftParentHash()) == null) ||
                (transactionData.getRightParentHash() != null && transactions.getByHash(transactionData.getRightParentHash()) == null);
    }

    public int totalPostponedTransactions() {
        return postponedTransactions.size();
    }

    @Override
    public void resetOldClusterStampTransactions() {
        if (clusterStampService.shouldUpdateClusterStampDBVersion()) {
            if (clusterStampService.isClusterStampDBVersionExist() && networkService.getRecoveryServerAddress() != null) {
                log.info("Starting to reset old clusterstamp transactions");
                databaseConnector.resetTransactionColumnFamilies();
                log.info("Finished to reset old clusterstamp transactions");
            }
            clusterStampService.setClusterStampDBVersion();
        }
    }
}
