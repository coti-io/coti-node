package io.coti.storagenode.controllers;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.http.AddEntitiesBulkRequest;
import io.coti.basenode.http.GetHistoryTransactionsRequest;
import io.coti.basenode.http.data.GetHashToPropagatable;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.storagenode.services.TransactionStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import static org.springframework.web.bind.annotation.RequestMethod.PUT;


@Slf4j
@RestController
public class TransactionController {

    @Autowired
    private TransactionStorageService transactionStorageService;

    @RequestMapping(value = "/transactions", method = PUT)
    public ResponseEntity<IResponse> storeMultipleTransactionsToStorage(@Valid @RequestBody AddEntitiesBulkRequest addEntitiesBulkRequest) {
        return transactionStorageService.storeMultipleObjectsToStorage(addEntitiesBulkRequest.getHashToEntityJsonDataMap());
    }

    @PostMapping(value = "/transactions", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.APPLICATION_JSON_UTF8_VALUE})
    public void getMultipleTransactionsInBlocksFromStorage(@Valid @RequestBody GetHistoryTransactionsRequest getHistoryTransactionsRequest, HttpServletResponse response) {
        transactionStorageService.retrieveMultipleObjectsInBlocksFromStorage(getHistoryTransactionsRequest, response);
    }

    @PostMapping(value = "/transactions/reactive", produces = MediaType.APPLICATION_STREAM_JSON_VALUE, consumes = MediaType.APPLICATION_STREAM_JSON_VALUE)
    public Flux<GetHashToPropagatable<TransactionData>> getTransactionsReactive(@RequestBody Flux<Hash> transactionHashes) {
        Flux<GetHashToPropagatable<TransactionData>> flux = Flux.empty();
        transactionHashes.subscribe(transactionHash -> flux.push(sink -> sink.next(transactionStorageService.retrieveHashToObjectFromStorage(transactionHash))));
        return flux;

    }
}
