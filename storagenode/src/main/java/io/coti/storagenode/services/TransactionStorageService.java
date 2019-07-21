package io.coti.storagenode.services;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.http.EntitiesBulkJsonResponse;
import io.coti.basenode.http.GetHashToTransactionResponse;
import io.coti.basenode.http.GetHistoryTransactionsRequest;
import io.coti.basenode.http.GetHistoryTransactionsResponse;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.services.BaseNodeValidationService;
import io.coti.storagenode.data.enums.ElasticSearchData;
import io.coti.storagenode.model.ObjectService;
import io.coti.storagenode.services.interfaces.ITransactionStorageValidationService;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@Slf4j
@Service
public class TransactionStorageService extends EntityStorageService implements ITransactionStorageValidationService
{

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


    private ObjectMapper mapper;
    private BlockingQueue<GetHashToTransactionResponse> retrievedTransactions;
    private Thread retrievedTransactionsThread;
    private ThreadPoolExecutor executorPool;

    @PostConstruct
    public void init()
    {
//        mapper = new ObjectMapper();
        mapper = new ObjectMapper()
                .registerModule(new ParameterNamesModule())
                .registerModule(new Jdk8Module())
                .registerModule(new JavaTimeModule()); // new module, NOT JSR310Module
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        super.objectType = ElasticSearchData.TRANSACTIONS;

        //TODO 7/15/2019 tomer:
        retrievedTransactions = new LinkedBlockingDeque<>();
//        retrievedTransactionsThread = new Thread(this::updateRetrievedTransactions);
//        retrievedTransactionsThread.start();

        ThreadFactory threadFactory = Executors.defaultThreadFactory();
        RejectedExecutionHandlerRetrievingBlocks rejectionHandler = new RejectedExecutionHandlerRetrievingBlocks();

        executorPool = new
                ThreadPoolExecutor(CORE_POOL_SIZE,MAX_POOL_SIZE,KEEP_ALIVE_TIME, TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(BLOCKING_QUEUE_CAPACITY), threadFactory, rejectionHandler);
    }



    public boolean isObjectDIOK(Hash objectHash, String txAsJson)
    {
        //Check for Data Integrity of the Tx
        TransactionData transactionDataDeserializedFromES = null;
        try {
            transactionDataDeserializedFromES = mapper.readValue(txAsJson, TransactionData.class);
            int temp = 7;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        boolean valid = true; //TODO: Disabled for testing purposes
//        boolean valid = validationService.validateTransactionDataIntegrity(transactionDataDeserializedFromES);
        if(!valid) {
            log.error("Failed verification for Transaction Data {}",transactionDataDeserializedFromES.getHash() );
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

    public ResponseEntity<IResponse> retrieveMultipleObjectsFromStorage(GetHistoryTransactionsRequest getHistoryTransactionsRequest) {
        return super.retrieveMultipleObjectsFromStorage(getHistoryTransactionsRequest.getHashes());
    }

    public void retrieveMultipleObjectsInBlocksFromStorage(GetHistoryTransactionsRequest getHistoryTransactionsRequest, HttpServletResponse response) {
        retrieveMultipleTransactionsInBlocksFromStorage(getHistoryTransactionsRequest.getHashes(), new GetHistoryTransactionsResponse(), response);
}

    private void retrieveMultipleTransactionsInBlocksFromStorage(List<Hash> hashes, GetHistoryTransactionsResponse getHistoryTransactionsResponse, HttpServletResponse response) {
        List<List<Hash>> blocksOfHashes = divideHashesToBlocks(hashes);
        if(blocksOfHashes==null) {
            queueTransactionsDataBlock(new GetHistoryTransactionsResponse(new HashMap<>()), HttpStatus.OK);
            return;
        }

        int blocksOfHashesAmount = blocksOfHashes.size();
        for( int blockNumber = 0 ; blockNumber < blocksOfHashesAmount ; blockNumber++) {
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
        try{
            if(!executorPool.awaitTermination(500, TimeUnit.MILLISECONDS)) {
                executorPool.shutdown();
            }
        } catch (InterruptedException e) {
            executorPool.shutdown();
        }

    }



    private void getTransactionsDataBlock(GetHistoryTransactionsResponse getHistoryTransactionsResponse, List<List<Hash>> blocksOfHashes, int blockNumber) {
        List<Hash> blockHashes = blocksOfHashes.get(blockNumber);
        HashMap<Hash, String> responsesMap = new HashMap<>();
        ResponseEntity<IResponse> objectsByHashResponse = objectService.getMultiObjectsFromDb(blockHashes, false, objectType);
        // For Unsuccessful retrieval of data
        if( !isResponseOK(objectsByHashResponse)){
            responsesMap.put(null,null);
            getHistoryTransactionsResponse.setEntitiesBulkResponses(responsesMap);
            //TODO 7/14/2019 tomer: Add to blocking queue
            queueTransactionsDataBlock(getHistoryTransactionsResponse, objectsByHashResponse.getStatusCode());
            return;
        }

        // For successfully retrieved data, perform also data-integrity checks
        verifyEntitiesFromDbMap(responsesMap, objectsByHashResponse);
        getHistoryTransactionsResponse.setEntitiesBulkResponses(responsesMap);
        //TODO 7/14/2019 tomer: Add to blocking queue
        queueTransactionsDataBlock(getHistoryTransactionsResponse, HttpStatus.OK);
    }

    private List<List<Hash>> divideHashesToBlocks(List<Hash> hashes) {
        if( hashes == null || hashes.isEmpty() ) {
            return null;
        }
        List<List<Hash>> hashesBlocks = ListUtils.partition(hashes, BLOCK_SIZE);
        return hashesBlocks;
    }

    private ResponseEntity<IResponse> queueTransactionsDataBlock(GetHistoryTransactionsResponse getHistoryTransactionsResponse, HttpStatus httpStatus) {
        if(getHistoryTransactionsResponse ==null || getHistoryTransactionsResponse.getEntitiesBulkResponses() == null || !getHistoryTransactionsResponse.getEntitiesBulkResponses().isEmpty()) {
            return ResponseEntity.status(httpStatus).body(getHistoryTransactionsResponse);
        }
        getHistoryTransactionsResponse.getEntitiesBulkResponses().entrySet().forEach(entry -> {
            queueTransactionData(entry);
        });


        return ResponseEntity.status(httpStatus).body(getHistoryTransactionsResponse);
    }

    private void queueTransactionData(Map.Entry<Hash, String> entry) {
        TransactionData transactionData = null;
        try {
            transactionData = mapper.readValue(entry.getValue(), TransactionData.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        GetHashToTransactionResponse transactionDataPair = new GetHashToTransactionResponse(entry.getKey(), transactionData);

        try {
            retrievedTransactions.put(transactionDataPair);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }


    private class WorkerThread implements Runnable {
        GetHistoryTransactionsResponse getHistoryTransactionsResponse;
        List<List<Hash>> blocksOfHashes;
        int blockNumber;

        public WorkerThread(GetHistoryTransactionsResponse getHistoryTransactionsResponse, List<List<Hash>> blocksOfHashes, int blockNumber) {
            this.getHistoryTransactionsResponse = getHistoryTransactionsResponse;
            this.blocksOfHashes = blocksOfHashes;
            this.blockNumber = blockNumber;
        }

        @Override
        public void run() {
            System.out.println(Thread.currentThread().getName()+" Starting block number = "+blockNumber);
            getTransactionsDataBlock(getHistoryTransactionsResponse, blocksOfHashes, blockNumber);
            System.out.println(Thread.currentThread().getName()+" End ");

        }
    }

    private class RejectedExecutionHandlerRetrievingBlocks implements RejectedExecutionHandler {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            System.out.println(r.toString() + " is rejected");
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
        while(!Thread.currentThread().isInterrupted()) {
            try {
                GetHashToTransactionResponse transactionDataPair = retrievedTransactions.take();
                chunkingService.getTransaction(transactionDataPair, output);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        LinkedList<GetHashToTransactionResponse> remainingTransactionDataPairs = new LinkedList<>();
        retrievedTransactions.drainTo(remainingTransactionDataPairs);
        if(!remainingTransactionDataPairs.isEmpty()) {
            for (GetHashToTransactionResponse remainingTransactionDataPair : remainingTransactionDataPairs) {
                chunkingService.getTransaction(remainingTransactionDataPair, output);
            }
        }
    }
}
