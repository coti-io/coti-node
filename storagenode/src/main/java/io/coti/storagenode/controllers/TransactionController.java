package io.coti.storagenode.controllers;

import io.coti.basenode.data.TransactionData;
import io.coti.basenode.http.AddEntitiesBulkRequest;
import io.coti.basenode.http.GetHistoryTransactionsRequest;
import io.coti.basenode.http.data.GetHashToPropagatable;
import io.coti.basenode.http.interfaces.IResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import static io.coti.storagenode.services.NodeServiceManager.transactionStorageService;

@RestController
public class TransactionController {

    @PutMapping(value = "/transactions")
    public ResponseEntity<IResponse> storeMultipleTransactionsToStorage(@Valid @RequestBody AddEntitiesBulkRequest addEntitiesBulkRequest) {
        return transactionStorageService.storeMultipleObjectsToStorage(addEntitiesBulkRequest.getHashToEntityJsonDataMap());
    }

    @PostMapping(value = "/transactions", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public void getMultipleTransactionsInBlocksFromStorage(@Valid @RequestBody GetHistoryTransactionsRequest getHistoryTransactionsRequest, HttpServletResponse response) {
        transactionStorageService.retrieveMultipleObjectsInBlocksFromStorage(getHistoryTransactionsRequest, response);
    }

    @PostMapping(value = "/transactions/reactive", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<GetHashToPropagatable<TransactionData>> getTransactionsReactive(@Valid @RequestBody GetHistoryTransactionsRequest getHistoryTransactionsRequest) {
        return Flux.create(fluxSink -> transactionStorageService.retrieveMultipleObjectsInReactiveFromStorage(getHistoryTransactionsRequest, fluxSink));


    }
}
