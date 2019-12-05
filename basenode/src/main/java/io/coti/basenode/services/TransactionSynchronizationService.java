package io.coti.basenode.services;

import io.coti.basenode.communication.JacksonSerializer;
import io.coti.basenode.data.AddressTransactionsHistory;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
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
public class TransactionSynchronizationService implements ITransactionSynchronizationService {

    private static final String RECOVERY_NODE_GET_BATCH_ENDPOINT = "/transaction_batch";
    private static final String STARTING_INDEX_URL_PARAM_ENDPOINT = "?starting_index=";
    private static final long MAXIMUM_BUFFER_SIZE = 300000;
    @Autowired
    private ITransactionHelper transactionHelper;
    @Autowired
    private ITransactionService transactionService;
    @Autowired
    private IClusterService clusterService;
    @Autowired
    private INetworkService networkService;
    @Autowired
    private AddressTransactionsHistories addressTransactionsHistories;
    @Autowired
    private JacksonSerializer jacksonSerializer;
    @Autowired
    private RestTemplate restTemplate;

    public void requestMissingTransactions(long firstMissingTransactionIndex) {
        try {
            log.info("Starting to get missing transactions");
            List<TransactionData> missingTransactions = new ArrayList<>();
            Set<Hash> trustChainUnconfirmedExistingTransactionHashes = clusterService.getTrustChainConfirmationTransactionHashes();
            AtomicLong completedMissingTransactionNumber = new AtomicLong(0);
            AtomicLong receivedMissingTransactionNumber = new AtomicLong(0);
            AtomicBoolean finishedToReceive = new AtomicBoolean(false);
            Thread monitorMissingTransactionThread = transactionService.monitorTransactionThread("missing", completedMissingTransactionNumber, receivedMissingTransactionNumber);
            Thread insertMissingTransactionThread = insertMissingTransactionThread(missingTransactions, trustChainUnconfirmedExistingTransactionHashes, completedMissingTransactionNumber, monitorMissingTransactionThread, finishedToReceive);
            ResponseExtractor responseExtractor = response -> {
                byte[] buf = new byte[Math.toIntExact(MAXIMUM_BUFFER_SIZE)];
                int offset = 0;
                int n;
                while ((n = response.getBody().read(buf, offset, buf.length - offset)) > 0) {
                    try {
                        TransactionData missingTransaction = jacksonSerializer.deserialize(buf);
                        if (missingTransaction != null) {
                            missingTransactions.add(missingTransaction);
                            receivedMissingTransactionNumber.incrementAndGet();
                            if (!insertMissingTransactionThread.isAlive()) {
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
            restTemplate.execute(networkService.getRecoveryServerAddress() + RECOVERY_NODE_GET_BATCH_ENDPOINT
                    + STARTING_INDEX_URL_PARAM_ENDPOINT + firstMissingTransactionIndex, HttpMethod.GET, null, responseExtractor);
            if (insertMissingTransactionThread.isAlive()) {
                log.info("Received all {} missing transactions from recovery server", receivedMissingTransactionNumber);
                synchronized (finishedToReceive) {
                    finishedToReceive.set(true);
                    finishedToReceive.wait();
                }
            }
            log.info("Finished to get missing transactions");
        } catch (TransactionSyncException e) {
            throw new TransactionSyncException("Error at missing transactions from recovery Node.\n" + e.getMessage(), e);
        } catch (Exception e) {
            throw new TransactionSyncException("Error at missing transactions from recovery Node", e);
        }

    }

    private Thread insertMissingTransactionThread(List<TransactionData> missingTransactions, Set<Hash> trustChainUnconfirmedExistingTransactionHashes, AtomicLong completedMissingTransactionNumber, Thread monitorMissingTransactionThread, AtomicBoolean finishedToReceive) throws Exception {
        return new Thread(() -> {
            Map<Hash, AddressTransactionsHistory> addressToTransactionsHistoryMap = new ConcurrentHashMap<>();
            int offset = 0;
            int nextOffSet;
            int missingTransactionsSize;
            monitorMissingTransactionThread.start();

            while ((missingTransactionsSize = missingTransactions.size()) > offset || !finishedToReceive.get()) {
                if (missingTransactionsSize - 1 > offset || (missingTransactionsSize - 1 == offset && missingTransactions.get(offset) != null)) {
                    nextOffSet = offset + (finishedToReceive.get() ? missingTransactionsSize - offset : 1);
                    for (int i = offset; i < nextOffSet; i++) {
                        TransactionData transactionData = missingTransactions.get(i);
                        transactionService.handleMissingTransaction(transactionData, trustChainUnconfirmedExistingTransactionHashes);
                        transactionHelper.updateAddressTransactionHistory(addressToTransactionsHistoryMap, transactionData);
                        missingTransactions.set(i, null);
                        completedMissingTransactionNumber.incrementAndGet();
                    }
                    offset = nextOffSet;
                }
            }
            addressTransactionsHistories.putBatch(addressToTransactionsHistoryMap);
            monitorMissingTransactionThread.interrupt();
            synchronized (finishedToReceive) {
                finishedToReceive.notify();
            }

        });

    }

}
