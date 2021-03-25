package io.coti.basenode.services;

import io.coti.basenode.communication.JacksonSerializer;
import io.coti.basenode.data.*;
import io.coti.basenode.exceptions.TransactionSyncException;
import io.coti.basenode.model.AddressTransactionsHistories;
import io.coti.basenode.services.interfaces.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Slf4j
public class BaseNodeTransactionSynchronizationService implements ITransactionSynchronizationService {

    private static final String RECOVERY_NODE_GET_BATCH_ENDPOINT = "/transaction_batch";
    private static final String STARTING_INDEX_URL_PARAM_ENDPOINT = "?starting_index=";
    private static final long MAXIMUM_BUFFER_SIZE = 300000;
    @Autowired
    protected ITransactionHelper transactionHelper;
    @Autowired
    private ITransactionService transactionService;
    @Autowired
    private IClusterService clusterService;
    @Autowired
    private INetworkService networkService;
    @Autowired
    protected AddressTransactionsHistories addressTransactionsHistories;
    @Autowired
    private JacksonSerializer jacksonSerializer;
    @Autowired
    private RestTemplate restTemplate;
    private final Object finishLock = new Object();
    protected EnumMap<InitializationTransactionHandlerType, ExecutorData> missingTransactionExecutorMap;

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
            restTemplate.execute(networkService.getRecoveryServerAddress() + RECOVERY_NODE_GET_BATCH_ENDPOINT
                    + STARTING_INDEX_URL_PARAM_ENDPOINT + firstMissingTransactionIndex, HttpMethod.GET, null, responseExtractor);
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
        } catch (Exception e) {
            throw new TransactionSyncException("Error at missing transactions from recovery Node", e);
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
                            EnumSet.allOf(InitializationTransactionHandlerType.class).forEach(initializationTransactionHandlerType -> missingTransactionExecutorMap.put(initializationTransactionHandlerType, new ExecutorData()));
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
            missingTransactionExecutorMap.forEach((initializationTransactionHandlerType, executorData) -> executorData.waitForTermination());

            monitorMissingTransactionThread.interrupt();
            try {
                monitorMissingTransactionThread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            synchronized (finishLock) {
                finishedToInsert.set(true);
                finishLock.notifyAll();
            }

        });

    }

    protected void insertMissingTransactions(List<TransactionData> missingTransactions, Set<Hash> trustChainUnconfirmedExistingTransactionHashes, AtomicLong completedMissingTransactionNumber, AtomicBoolean finishedToReceive, int offset) {
        int missingTransactionsSize;
        int nextOffSet;
        Map<Hash, AddressTransactionsHistory> addressToTransactionsHistoryMap = new ConcurrentHashMap<>();
        while ((missingTransactionsSize = missingTransactions.size()) > offset || !finishedToReceive.get()) {
            if (missingTransactionsSize - 1 > offset || (missingTransactionsSize - 1 == offset && missingTransactions.get(offset) != null)) {
                nextOffSet = offset + (finishedToReceive.get() ? missingTransactionsSize - offset : 1);
                for (int i = offset; i < nextOffSet; i++) {
                    TransactionData transactionData = missingTransactions.get(i);
                    transactionService.handleMissingTransaction(transactionData, trustChainUnconfirmedExistingTransactionHashes, missingTransactionExecutorMap);
                    transactionHelper.updateAddressTransactionHistory(addressToTransactionsHistoryMap, transactionData);
                    missingTransactions.set(i, null);
                    completedMissingTransactionNumber.incrementAndGet();
                }
                offset = nextOffSet;
            }
        }
        addressTransactionsHistories.putBatch(addressToTransactionsHistoryMap);
    }

}
