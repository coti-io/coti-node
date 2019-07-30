package io.coti.fullnode.controllers;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.Response;
import io.coti.basenode.http.data.TransactionResponseData;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.services.TransactionIndexService;
import io.coti.fullnode.http.*;
import io.coti.fullnode.services.TransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/transaction")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private TransactionIndexService transactionIndexService;
    @Autowired
    private WebClient webClient;

    @PutMapping()
    public ResponseEntity<Response> addTransaction(@Valid @RequestBody AddTransactionRequest addTransactionRequest) {
        return transactionService.addNewTransaction(addTransactionRequest);
    }

    @PostMapping()
    public ResponseEntity<IResponse> getTransactionDetails(@Valid @RequestBody GetTransactionRequest getTransactionRequest) {
        return transactionService.getTransactionDetails(getTransactionRequest.transactionHash);
    }

    @PostMapping(value = "/addressTransactions")
    public ResponseEntity<IResponse> getAddressTransactions(@Valid @RequestBody AddressRequest addressRequest) {
        return transactionService.getAddressTransactions(addressRequest.getAddress());
    }

    @PostMapping(value = "/addressTransactions/batch")
    public void getAddressTransactionBatch(@Valid @RequestBody GetAddressTransactionBatchRequest getAddressTransactionBatchRequest, HttpServletResponse response) {
        transactionService.getAddressTransactionBatch(getAddressTransactionBatchRequest, response);
    }

    @PostMapping(value = "/addressTransactions/batch/reactive", produces = MediaType.APPLICATION_STREAM_JSON_VALUE, consumes = MediaType.APPLICATION_STREAM_JSON_VALUE)
    public Flux<TransactionResponseData> getAddressTransactionBatchReactive(@RequestBody Flux<Hash> transactionHashes) {
        Flux<TransactionResponseData> flux = Flux.empty();
        transactionHashes.subscribe(transactionHash -> flux.push(sink -> {
            TransactionResponseData transactionResponseData = transactionService.retrieveTransactionByHash(transactionHash);
            if (transactionResponseData != null) {
                sink.next(transactionService.retrieveTransactionByHash(transactionHash));
            }
        }));
        return flux;
    }

    @PostMapping(value = "/transactions", produces = MediaType.APPLICATION_STREAM_JSON_VALUE)
    public Flux<TransactionResponseData> getTransactions(@RequestBody @Valid GetTransactionsRequest getTransactionsRequest) {
        Flux<Hash> hashes = Flux.fromIterable(getTransactionsRequest.getTransactionHashes());
        return webClient.post().uri("http://localhost:7070/transaction/addressTransactions/batch/reactive").contentType(MediaType.APPLICATION_STREAM_JSON)
                .body(BodyInserters.fromPublisher(hashes, Hash.class))
                .retrieve().bodyToFlux(TransactionResponseData.class);
    }

    {

    }

    @GetMapping(value = "/lastTransactions")
    public ResponseEntity<IResponse> getLastTransactions() {
        return transactionService.getLastTransactions();
    }

    @GetMapping(value = "/index")
    public ResponseEntity getCurrentIndex() {
        return ResponseEntity.ok(transactionIndexService.getLastTransactionIndexData());
    }
}