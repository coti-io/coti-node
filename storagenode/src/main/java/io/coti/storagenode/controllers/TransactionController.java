package io.coti.storagenode.controllers;

import io.coti.basenode.http.AddEntitiesBulkRequest;
import io.coti.basenode.http.AddEntityRequest;
import io.coti.basenode.http.GetTransactionsBulkRequest;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.storagenode.services.ChunkingService;
import io.coti.storagenode.services.TransactionStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;


@Slf4j
@RestController
public class TransactionController {

    @Autowired
    private TransactionStorageService transactionStorageService;

    @Autowired
    private ChunkingService chunkingService;


    @RequestMapping(value = "/transactions", method = PUT)
    public ResponseEntity<IResponse> storeMultipleTransactionsToStorage(@Valid @RequestBody AddEntitiesBulkRequest addEntitiesBulkRequest) {
        return transactionStorageService.storeMultipleObjectsToStorage(addEntitiesBulkRequest.getHashToEntityJsonDataMap());
    }

    @RequestMapping(value = "/transactions", method = POST)
        public ResponseEntity<IResponse> getMultipleTransactionsFromStorage(@Valid @RequestBody GetTransactionsBulkRequest getTransactionsBulkRequest) {
        ResponseEntity<IResponse> responseResponseEntity = transactionStorageService.retrieveMultipleObjectsFromStorage(getTransactionsBulkRequest);
        return responseResponseEntity;
    }

    @RequestMapping(value = "/transactionsInBlocks", method = POST)
    public ResponseEntity<IResponse> getMultipleTransactionsInBlocksFromStorage(@Valid @RequestBody GetTransactionsBulkRequest getTransactionsBulkRequest) {
        ResponseEntity<IResponse> responseResponseEntity = transactionStorageService.retrieveMultipleObjectsInBlocksFromStorage(getTransactionsBulkRequest);
        return responseResponseEntity;
    }

//    //TODO 7/10/2019 astolia: Dummy controller for testing
//    @GetMapping(value = "/transactionsBatch")
//    public void getMultipleTransactionsFromStorageBatched(HttpServletResponse response) throws IOException {
//        chunkingService.getTransactionBatch(response);
//    }

}
