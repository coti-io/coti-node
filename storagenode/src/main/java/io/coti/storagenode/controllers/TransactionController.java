package io.coti.storagenode.controllers;

import io.coti.basenode.http.*;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.storagenode.data.enums.ElasticSearchData;
import io.coti.storagenode.services.TransactionStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static org.springframework.web.bind.annotation.RequestMethod.*;


@Slf4j
@RestController
public class TransactionController {

    @Autowired
    private TransactionStorageService transactionStorageService;

    //TODO: old implementation, verify this
    @RequestMapping(value = "/transaction", method = PUT)
    public ResponseEntity<IResponse> storeTransactionToStorage(@Valid @RequestBody AddEntityRequest addAddEntityRequest) {
        return transactionStorageService.storeObjectToStorage(addAddEntityRequest.getHash(),
                addAddEntityRequest.getEntityJson());
    }

    //TODO: old implementation, verify this
    @RequestMapping(value = "/transaction", method = GET)
    public ResponseEntity<IResponse> getTransactionFromStorage(@Valid @RequestBody GetEntityRequest getEntityRequest) {
        return transactionStorageService.retrieveObjectFromStorage(getEntityRequest.getHash(), ElasticSearchData.TRANSACTIONS
                );
    }


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

}
