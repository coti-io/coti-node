package io.coti.storagenode.controllers;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.*;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.storagenode.services.TransactionStorageValidationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

//import static io.coti.storagenode.services.AddressStorageValidationService.ADDRESS_TRANSACTION_HISTORY_OBJECT_NAME;
import static io.coti.storagenode.services.TransactionStorageValidationService.TRANSACTION_OBJECT_NAME;
import static org.springframework.web.bind.annotation.RequestMethod.*;


@Slf4j
@RestController
public class TransactionController {

    @Autowired
    private TransactionStorageValidationService transactionStorageValidationService;

    //TODO: old implementation, verify this
    @RequestMapping(value = "/transaction", method = PUT)
    public ResponseEntity<IResponse> storeTransactionToStorage(@Valid @RequestBody AddEntityRequest addAddEntityRequest) {
        return transactionStorageValidationService.storeObjectToStorage(addAddEntityRequest.getHash(),
                addAddEntityRequest.getEntityJson());
    }

    //TODO: old implementation, verify this
    @RequestMapping(value = "/transaction", method = GET)
    public ResponseEntity<IResponse> getTransactionFromStorage(@Valid @RequestBody GetEntityRequest getEntityRequest) {
        return transactionStorageValidationService.retrieveObjectFromStorage(getEntityRequest.getHash(), TRANSACTION_OBJECT_NAME
                );
    }

    //TODO: old implementation, verify this
    @RequestMapping(value = "/transactions", method = PUT)
    public ResponseEntity<IResponse> storeMultipleTransactionsToStorage(@Valid @RequestBody AddEntitiesBulkRequest addEntitiesBulkRequest) {
        return transactionStorageValidationService.storeMultipleObjectsToStorage(addEntitiesBulkRequest.getHashToEntityJsonDataMap()
        );
    }

    @RequestMapping(value = "/transactions", method = POST)
        public ResponseEntity<IResponse> getMultipleTransactionsFromStorage(@Valid @RequestBody GetEntitiesBulkRequest getEntitiesBulkRequest) {
        ResponseEntity<IResponse> responseResponseEntity = transactionStorageValidationService.retrieveMultipleObjectsFromStorage(getEntitiesBulkRequest.getHashes(),
                TRANSACTION_OBJECT_NAME);
        return responseResponseEntity;
    }

}
