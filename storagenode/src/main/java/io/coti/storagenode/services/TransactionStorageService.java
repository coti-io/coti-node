package io.coti.storagenode.services;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.http.GetHistoryTransactionsRequest;
import io.coti.basenode.http.GetHistoryTransactionsResponse;
import io.coti.basenode.http.data.GetHashToPropagatable;
import io.coti.basenode.services.BaseNodeValidationService;
import io.coti.storagenode.data.enums.ElasticSearchData;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.FluxSink;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@Slf4j
@Service
public class TransactionStorageService extends EntityStorageService {

    private static final int BLOCK_SIZE = 100;
    private static final int POOL_MAX_SIZE = 20;
    @Autowired
    private BaseNodeValidationService validationService;

    @PostConstruct
    public void init() {
        super.objectType = ElasticSearchData.TRANSACTIONS;
    }


    public boolean validateObjectDataIntegrity(Hash objectHash, String txAsJson) {
        TransactionData transactionData = jacksonSerializer.deserialize(txAsJson);
        if (transactionData == null) {
            return false;
        }
        return validationService.validateTransactionDataIntegrity(transactionData);
    }

    @Override
    protected GetHistoryTransactionsResponse getEmptyEntitiesBulkResponse() {
        return new GetHistoryTransactionsResponse();
    }

    public void retrieveMultipleObjectsInReactiveFromStorage(GetHistoryTransactionsRequest getHistoryTransactionsRequest, FluxSink<GetHashToPropagatable<TransactionData>> sink) {
        try {
            getHistoryTransactionsRequest.getTransactionHashes().forEach(transactionHash -> sink.next(retrieveHashToObjectFromStorage(transactionHash)));
        } catch (Exception e) {
            log.error("{}: {}", e.getClass().getName(), e.getMessage());
        } finally {
            sink.complete();
        }
    }

    public void retrieveMultipleObjectsInBlocksFromStorage(GetHistoryTransactionsRequest getHistoryTransactionsRequest, HttpServletResponse response) {
        try {
            BlockingQueue<GetHashToPropagatable<TransactionData>> retrievedTransactionQueue = new LinkedBlockingQueue<>();

            List<Hash> transactionHashes = getHistoryTransactionsRequest.getTransactionHashes();
            List<List<Hash>> blocksOfHashes = divideHashesToBlocks(transactionHashes);
            if (blocksOfHashes.isEmpty()) {
                return;
            }
            ExecutorService executorPool = Executors.newFixedThreadPool(Math.min(blocksOfHashes.size(), POOL_MAX_SIZE));

            for (int blockNumber = 0; blockNumber < blocksOfHashes.size(); blockNumber++) {
                Runnable worker = new WorkerThread(blocksOfHashes.get(blockNumber), blockNumber, retrievedTransactionQueue);
                executorPool.execute(worker);
            }

            int uncompletedTransactionCounter = transactionHashes.size();

            OutputStream output = response.getOutputStream();

            handleRetrievedTransactionsFromElasticSearch(retrievedTransactionQueue, uncompletedTransactionCounter, output);

            executorPool.shutdown();
            awaitTerminationForExecutorPool(executorPool);
        } catch (Exception e) {
            log.error("{}: {}", e.getClass().getName(), e.getMessage());
        }

    }

    private void handleRetrievedTransactionsFromElasticSearch(BlockingQueue<GetHashToPropagatable<TransactionData>> retrievedTransactionQueue, int uncompletedTransactionCounter, OutputStream output) throws IOException {
        while (!Thread.currentThread().isInterrupted() && uncompletedTransactionCounter > 0) {
            try {
                GetHashToPropagatable<TransactionData> getHashToTransactionData = retrievedTransactionQueue.take();
                output.write(jacksonSerializer.serialize(getHashToTransactionData));
                output.flush();
                uncompletedTransactionCounter--;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void awaitTerminationForExecutorPool(ExecutorService executorPool) {
        try {
            if (!executorPool.awaitTermination(500, TimeUnit.MILLISECONDS)) {
                executorPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private List<List<Hash>> divideHashesToBlocks(List<Hash> hashes) {
        if (hashes == null || hashes.isEmpty()) {
            return new ArrayList<>();
        }
        return ListUtils.partition(hashes, BLOCK_SIZE);
    }

    private class WorkerThread implements Runnable {
        private final List<Hash> transactionHashes;
        private final int blockNumber;
        private final BlockingQueue<GetHashToPropagatable<TransactionData>> retrievedTransactionQueue;

        public WorkerThread(List<Hash> transactionHashes, int blockNumber, BlockingQueue<GetHashToPropagatable<TransactionData>> retrievedTransactionQueue) {
            this.transactionHashes = transactionHashes;
            this.blockNumber = blockNumber;
            this.retrievedTransactionQueue = retrievedTransactionQueue;
        }

        @Override
        public void run() {
            log.info("Thread {} ,Starting block number = {}", Thread.currentThread().getId(), blockNumber);
            getTransactionsDataBlock(transactionHashes, retrievedTransactionQueue);
            log.info("Thread {} ,Ended block number = {}", Thread.currentThread().getId(), blockNumber);
        }

        private void getTransactionsDataBlock(List<Hash> transactionHashes, BlockingQueue<GetHashToPropagatable<TransactionData>> retrievedTransactionQueue) {

            try {
                Map<Hash, String> transactionsMap = retrieveMultipleObjectsFromStorage(transactionHashes);

                queueTransactionsDataBlock(transactionsMap, retrievedTransactionQueue);
            } catch (Exception e) {
                log.error("{}: {}", e.getClass().getName(), e.getMessage());
            }

        }

        private void queueTransactionsDataBlock(Map<Hash, String> transactionMap, BlockingQueue<GetHashToPropagatable<TransactionData>> retrievedTransactionQueue) {

            transactionMap.forEach((key, value) -> {
                TransactionData transactionData = jacksonSerializer.deserialize(value);
                GetHashToPropagatable<TransactionData> transactionDataPair = new GetHashToPropagatable<>(key, transactionData);
                try {
                    retrievedTransactionQueue.put(transactionDataPair);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
    }
}
