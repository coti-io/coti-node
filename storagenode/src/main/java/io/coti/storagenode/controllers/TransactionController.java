package io.coti.storagenode.controllers;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.http.AddEntityRequest;
import io.coti.basenode.http.AddEntitiesBulkRequest;
import io.coti.basenode.http.GetEntityRequest;
import io.coti.basenode.http.GetEntitiesBulkRequest;
import io.coti.storagenode.services.TransactionStorageValidationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Map;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;


@Slf4j
@RestController
public class TransactionController {

    @Autowired
    private TransactionStorageValidationService transactionStorageValidationService;

    @RequestMapping(value = "/transaction", method = PUT)
    public ResponseEntity<IResponse> storeTransactionToStorage(@Valid @RequestBody AddEntityRequest addAddEntityRequest) {
        return transactionStorageValidationService.storeObjectToStorage(addAddEntityRequest.getHash(),
                addAddEntityRequest.getEntityJson(), addAddEntityRequest.getHistoryNodeConsensusResult() );
    }

    @RequestMapping(value = "/transaction", method = GET)
    public ResponseEntity<IResponse> getTransactionFromStorage(@Valid @RequestBody GetEntityRequest getEntityRequest) {
        return transactionStorageValidationService.retrieveObjectFromStorage(getEntityRequest.getHash(),
                getEntityRequest.getHistoryNodeConsensusResult());
    }


    @RequestMapping(value = "/transactions", method = PUT)
    public ResponseEntity<IResponse> storeMultipleTransactionsToStorage(@Valid @RequestBody AddEntitiesBulkRequest addEntitiesBulkRequest) {
        return transactionStorageValidationService.storeMultipleObjectsToStorage(addEntitiesBulkRequest.getHashToEntityJsonDataMap(),
                addEntitiesBulkRequest.getHistoryNodeConsensusResult() );
    }

    @RequestMapping(value = "/transactions", method = GET)
    public Map<Hash, ResponseEntity<IResponse>> getMultipleTransactionsFromStorage(@Valid @RequestBody GetEntitiesBulkRequest getEntitiesBulkRequest) {
        return transactionStorageValidationService.retrieveMultipleObjectsFromStorage(getEntitiesBulkRequest.getHashes(),
                getEntitiesBulkRequest.getHistoryNodeConsensusResult());
    }


}
