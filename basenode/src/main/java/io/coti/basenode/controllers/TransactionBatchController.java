package io.coti.basenode.controllers;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.GetTransactionRequest;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.services.interfaces.ITransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.IOException;

@Slf4j
@RestController
public class TransactionBatchController {

    @Autowired
    private ITransactionService transactionService;

    @GetMapping(value = "/transaction_batch")
    public void getTransactionBatch(@RequestParam @Valid @NotNull Long starting_index, HttpServletResponse response) throws IOException {
        transactionService.getTransactionBatch(starting_index, response);
    }

    @GetMapping(value = "/transaction/hash")
    public void getSingleTransaction(@RequestParam @Valid @NotNull Hash hash, HttpServletResponse response) throws IOException {
        transactionService.getSingleTransaction(hash, response);
    }

    @GetMapping(value = "/transaction_batch/reactive", produces = MediaType.APPLICATION_STREAM_JSON_VALUE)
    public Flux<byte[]> getTransactionBatchReactive(@RequestParam @Valid @NotNull Long starting_index) {
        return Flux.create(fluxSink -> transactionService.getTransactionBatch(starting_index, fluxSink));
    }

    @PostMapping(value = "/transaction/hash")
    public ResponseEntity<IResponse> getSingleTransaction(@RequestBody @Valid GetTransactionRequest getTransactionRequest) {
        return transactionService.getSingleTransaction(getTransactionRequest);
    }
}
