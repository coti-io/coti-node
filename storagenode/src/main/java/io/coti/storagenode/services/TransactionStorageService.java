package io.coti.storagenode.services;

import io.coti.basenode.communication.JacksonSerializer;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.http.EntitiesBulkJsonResponse;
import io.coti.basenode.http.GetHistoryTransactionsRequest;
import io.coti.basenode.http.GetHistoryTransactionsResponse;
import io.coti.basenode.http.data.GetHashToTransactionData;
import io.coti.basenode.services.BaseNodeValidationService;
import io.coti.storagenode.data.enums.ElasticSearchData;
import io.coti.storagenode.services.interfaces.ITransactionStorageValidationService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@Slf4j
@Service
public class TransactionStorageService extends EntityStorageService implements ITransactionStorageValidationService {

    public static final int BLOCK_SIZE = 100;
    private static final int CORE_POOL_SIZE = 5;
    private static final int MAX_POOL_SIZE = 20;
    private static final int KEEP_ALIVE_TIME = 30;
    private static final int BLOCKING_QUEUE_CAPACITY = 1000;

    @Autowired
    private BaseNodeValidationService validationService;
    @Autowired
    private ObjectService objectService;
    @Autowired
    private ChunkingService chunkingService;
    @Autowired
    private JacksonSerializer jacksonSerializer;
    private BlockingQueue<GetHashToTransactionData> retrievedTransactions;
    private Thread retrievedTransactionsThread;
    private ThreadPoolExecutor executorPool;

    @PostConstruct
    public void init() {
        super.objectType = ElasticSearchData.TRANSACTIONS;

        //TODO 7/15/2019 tomer:
        retrievedTransactions = new LinkedBlockingDeque<>();
//        retrievedTransactionsThread = new Thread(this::updateRetrievedTransactions);
//        retrievedTransactionsThread.start();

        ThreadFactory threadFactory = Executors.defaultThreadFactory();
        RejectedExecutionHandlerRetrievingBlocks rejectionHandler = new RejectedExecutionHandlerRetrievingBlocks();
        executorPool = new
                ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE, KEEP_ALIVE_TIME, TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(BLOCKING_QUEUE_CAPACITY), threadFactory, rejectionHandler);
    }


    public boolean validateObjectDataIntegrity(Hash objectHash, String txAsJson) {
        TransactionData transactionData;

        transactionData = jacksonSerializer.deserialize(txAsJson);
        if (transactionData == null) {
            return false;
        }

        boolean valid = validationService.validateTransactionDataIntegrity(transactionData);
        if (!valid) {
            log.error("Failed verification for Transaction Data {}", transactionData.getHash());
        }
        return valid;
    }


    @Override
    protected GetHistoryTransactionsResponse getEmptyEntitiesBulkResponse() {
        return new GetHistoryTransactionsResponse();
    }

    @Override
    protected EntitiesBulkJsonResponse getEntitiesBulkResponse(Map<Hash, String> responsesMap) {
        EntitiesBulkJsonResponse entitiesBulkJsonResponse = new EntitiesBulkJsonResponse();
        entitiesBulkJsonResponse.setHashToEntitiesFromDbMap(responsesMap);
        return entitiesBulkJsonResponse;
    }

    public void retrieveMultipleObjectsInBlocksFromStorage(GetHistoryTransactionsRequest getHistoryTransactionsRequest, HttpServletResponse response) {
        List<Hash> transactionHashes = getHistoryTransactionsRequest.getTransactionHashes();
        List<List<Hash>> blocksOfHashes = divideHashesToBlocks(transactionHashes);

        for (int blockNumber = 0; blockNumber < blocksOfHashes.size(); blockNumber++) {
            Runnable worker = new WorkerThread(getHistoryTransactionsResponse, blocksOfHashes, blockNumber);
            executorPool.execute(worker);
        }

//        while(!Thread.currentThread().isInterrupted()) {
//            try {
//                Pair<Hash, TransactionData> hashTransactionDataPair = retrievedTransactions.take();
//
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }

        try {
            ServletOutputStream output = response.getOutputStream();
            retrievedTransactionsThread = new Thread(new updateRetrievedTransactionsThread(output));
            retrievedTransactionsThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        executorPool.shutdown();
        try {
            if (!executorPool.awaitTermination(500, TimeUnit.MILLISECONDS)) {
                executorPool.shutdown();
            }
        } catch (InterruptedException e) {
            executorPool.shutdown();
        }

    }


    private List<List<Hash>> divideHashesToBlocks(List<Hash> hashes) {
        if (hashes == null || hashes.isEmpty()) {
            return null;
        }
        List<List<Hash>> hashesBlocks = ListUtils.partition(hashes, BLOCK_SIZE);
        return hashesBlocks;
    }

    private void queueTransactionsDataBlock(Map<Hash, String> transactionMap) {
        if (getHistoryTransactionsResponse == null || getHistoryTransactionsResponse.getEntitiesBulkResponses() == null || !getHistoryTransactionsResponse.getEntitiesBulkResponses().isEmpty()) {
            return ResponseEntity.status(httpStatus).body(getHistoryTransactionsResponse);
        }
        getHistoryTransactionsResponse.getEntitiesBulkResponses().entrySet().forEach(entry -> {
            queueTransactionData(entry);
        });


        return ResponseEntity.status(httpStatus).body(getHistoryTransactionsResponse);
    }

    private void queueTransactionData(Map.Entry<Hash, String> entry) {
        TransactionData transactionData = jacksonSerializer.deserialize(entry.getValue());

        GetHashToTransactionData transactionDataPair = new GetHashToTransactionData(entry.getKey(), transactionData);

        try {
            retrievedTransactions.put(transactionDataPair);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }


    private class WorkerThread implements Runnable {
        private List<Hash> transactionHashes;
        private int blockNumber;

        public WorkerThread(List<Hash> transactionHashes, int blockNumber) {
            this.transactionHashes = transactionHashes;
            this.blockNumber = blockNumber;
        }

        @Override
        public void run() {
            log.info("Thread {} ,Starting block number = {}", Thread.currentThread().getId(), blockNumber);
            getTransactionsDataBlock(transactionHashes);
            log.info("Thread {} ,Ended block number = {}", Thread.currentThread().getId(), blockNumber);
        }

        private void getTransactionsDataBlock(List<Hash> transactionHashes) {

            try {
                Map<Hash, String> transactionsMap = objectService.getMultiObjectsFromDb(transactionHashes, false, objectType);

                queueTransactionsDataBlock(transactionsMap);
            } catch (Exception e) {
                log.error("{}: {}", e.getClass().getName(), e.getMessage());
            }

        }
    }
}

private class RejectedExecutionHandlerRetrievingBlocks implements RejectedExecutionHandler {
    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        log.info("Thread {} is rejected.", r.toString());
    }
}

private class updateRetrievedTransactionsThread implements Runnable {
    ServletOutputStream output;

    public updateRetrievedTransactionsThread(ServletOutputStream output) {
        this.output = output;
    }

    @Override
    public void run() {
        updateRetrievedTransactions(this.output);
    }

}

    private void updateRetrievedTransactions(ServletOutputStream output) {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                GetHashToTransactionData transactionDataPair = retrievedTransactions.take();
                chunkingService.getTransaction(transactionDataPair, output);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        LinkedList<GetHashToTransactionData> remainingTransactionDataPairs = new LinkedList<>();
        retrievedTransactions.drainTo(remainingTransactionDataPairs);
        if (!remainingTransactionDataPairs.isEmpty()) {
            for (GetHashToTransactionData remainingTransactionDataPair : remainingTransactionDataPairs) {
                chunkingService.getTransaction(remainingTransactionDataPair, output);
            }
        }
    }
}
