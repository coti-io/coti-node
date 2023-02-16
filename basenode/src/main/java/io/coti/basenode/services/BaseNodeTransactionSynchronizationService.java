package io.coti.basenode.services;

import io.coti.basenode.data.*;
import io.coti.basenode.exceptions.TransactionSyncException;
import io.coti.basenode.services.interfaces.ITransactionSynchronizationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResponseExtractor;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import static io.coti.basenode.services.BaseNodeServiceManager.*;

@Service
@Slf4j
public class BaseNodeTransactionSynchronizationService implements ITransactionSynchronizationService {

    private static final String RECOVERY_NODE_GET_BATCH_ENDPOINT = "/transaction_batch";
    private static final String STARTING_INDEX_URL_PARAM_ENDPOINT = "?starting_index=";
    private static final long MAXIMUM_BUFFER_SIZE = 300000;

    private final Object finishLock = new Object();
    private EnumMap<InitializationTransactionHandlerType, ExecutorData> missingTransactionExecutorMap;

    public synchronized void requestMissingTransactions(long firstMissingTransactionIndex) {
        try {
            log.info("Starting to get missing transactions");
            List<TransactionData> missingTransactions = new ArrayList<>();
            Set<Hash> trustChainUnconfirmedExistingTransactionHashes = clusterService.getTrustChainConfirmationTransactionHashes();
            AtomicLong completedMissingTransactionNumber = new AtomicLong(0);
            AtomicLong receivedMissingTransactionNumber = new AtomicLong(0);
            final AtomicBoolean finishedToReceive = new AtomicBoolean(false);
            final AtomicBoolean finishedToInsert = new AtomicBoolean(false);
            Thread monitorMissingTransactionThread = transactionService.monitorTransactionThread("missing", completedMissingTransactionNumber, receivedMissingTransactionNumber, "Sync Txs Monitor");
            Thread insertMissingTransactionThread = insertMissingTransactionThread(missingTransactions, trustChainUnconfirmedExistingTransactionHashes, completedMissingTransactionNumber, monitorMissingTransactionThread, finishedToReceive, finishedToInsert);
            ResponseExtractor<Void> responseExtractor = getResponseExtractorForMissingTransactionChunks(missingTransactions, receivedMissingTransactionNumber, insertMissingTransactionThread);
            restTemplate.execute(networkService.getRecoveryServerAddress() + RECOVERY_NODE_GET_BATCH_ENDPOINT + STARTING_INDEX_URL_PARAM_ENDPOINT + firstMissingTransactionIndex, HttpMethod.GET, null, responseExtractor);
            if (insertMissingTransactionThread.isAlive()) {
                log.info("Received all {} missing transactions from recovery server", receivedMissingTransactionNumber);
                synchronized (finishLock) {
                    finishedToReceive.set(true);
                    while (!finishedToInsert.get()) {
                        finishLock.wait(1000);
                    }
                }
            }
            log.info("Finished to get missing transactions");
        } catch (TransactionSyncException e) {
            throw new TransactionSyncException("Error at missing transactions from recovery Node.\n" + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new TransactionSyncException("Error at missing transactions from recovery Node.", e);
        } catch (Exception e) {
            throw new TransactionSyncException("Error at missing transactions from recovery Node.", e);
        }

    }

    private ResponseExtractor<Void> getResponseExtractorForMissingTransactionChunks(List<TransactionData> missingTransactions, AtomicLong receivedMissingTransactionNumber, Thread insertMissingTransactionThread) {
        return response -> {
            byte[] buf = new byte[Math.toIntExact(MAXIMUM_BUFFER_SIZE)];
            int offset = 0;
            int n;
            while ((n = response.getBody().read(buf, offset, buf.length - offset)) > 0) {
                try {
                    TransactionData missingTransaction = (TransactionData) jacksonSerializer.deserialize(buf);
                    if (missingTransaction != null) {
                        missingTransactions.add(missingTransaction);
                        receivedMissingTransactionNumber.incrementAndGet();
                        if (!insertMissingTransactionThread.isAlive()) {
                            missingTransactionExecutorMap = new EnumMap<>(InitializationTransactionHandlerType.class);
                            EnumSet.allOf(InitializationTransactionHandlerType.class).forEach(initializationTransactionHandlerType ->
                                    missingTransactionExecutorMap.put(initializationTransactionHandlerType, new ExecutorData(initializationTransactionHandlerType)));
                            insertMissingTransactionThread.start();
                        }
                        Arrays.fill(buf, 0, offset + n, (byte) 0);
                        offset = 0;
                    } else {
                        offset += n;
                    }

                } catch (Exception e) {
                    throw new TransactionSyncException("Error at getting chunks", e);
                }
            }
            return null;
        };
    }

    private Thread insertMissingTransactionThread(List<TransactionData> missingTransactions, Set<Hash> trustChainUnconfirmedExistingTransactionHashes, AtomicLong completedMissingTransactionNumber, Thread monitorMissingTransactionThread, final AtomicBoolean finishedToReceive, final AtomicBoolean finishedToInsert) {
        return new Thread(() -> {
            int offset = 0;
            monitorMissingTransactionThread.start();

            insertMissingTransactions(missingTransactions, trustChainUnconfirmedExistingTransactionHashes, completedMissingTransactionNumber, finishedToReceive, offset);

            monitorMissingTransactionThread.interrupt();
            try {
                monitorMissingTransactionThread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            missingTransactionExecutorMap.forEach((initializationTransactionHandlerType, executorData) -> executorData.waitForTermination(completedMissingTransactionNumber));
            synchronized (finishLock) {
                finishedToInsert.set(true);
                finishLock.notifyAll();
            }

        });

    }

    private void insertMissingTransactions(List<TransactionData> missingTransactions, Set<Hash> trustChainUnconfirmedExistingTransactionHashes, AtomicLong completedMissingTransactionNumber, AtomicBoolean finishedToReceive, int offset) {
        Map<Hash, AddressTransactionsHistory> addressToTransactionsHistoryMap = new ConcurrentHashMap<>();
        Consumer<TransactionData> handleTransactionConsumer = transactionData -> {
            transactionService.handleMissingTransaction(transactionData, trustChainUnconfirmedExistingTransactionHashes, missingTransactionExecutorMap);
            nodeTransactionHelper.updateAddressTransactionHistory(addressToTransactionsHistoryMap, transactionData);
        };
        handleMissingTransactions(missingTransactions, handleTransactionConsumer, completedMissingTransactionNumber, finishedToReceive, offset);

        insertAddressTransactionsHistory(addressToTransactionsHistoryMap);
    }

    private void handleMissingTransactions(List<TransactionData> missingTransactions, Consumer<TransactionData> handleTransactionConsumer, AtomicLong completedMissingTransactionNumber, AtomicBoolean finishedToReceive, int offset) {
        int missingTransactionsSize;
        int nextOffSet;
        while ((missingTransactionsSize = missingTransactions.size()) > offset || !finishedToReceive.get()) {
            if (missingTransactionsSize - 1 > offset || (missingTransactionsSize - 1 == offset && missingTransactions.get(offset) != null)) {
                nextOffSet = offset + (finishedToReceive.get() ? missingTransactionsSize - offset : 1);
                for (int i = offset; i < nextOffSet; i++) {
                    TransactionData transactionData = missingTransactions.get(i);
                    handleTransactionConsumer.accept(transactionData);
                    missingTransactions.set(i, null);
                    completedMissingTransactionNumber.incrementAndGet();
                }
                offset = nextOffSet;
            }
        }
    }

    private void insertAddressTransactionsHistory(Map<Hash, AddressTransactionsHistory> addressToTransactionsHistoryMap) {
        log.info("Starting to insert address transactions history");
        addressTransactionsHistories.putBatch(addressToTransactionsHistoryMap);
        log.info("Finished to insert address transactions history");
    }

}
