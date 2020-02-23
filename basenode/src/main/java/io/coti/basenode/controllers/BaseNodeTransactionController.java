package io.coti.basenode.controllers;

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

@Slf4j
@RestController
public class BaseNodeTransactionController {

    @Autowired
    private ITransactionService transactionService;

    @GetMapping(value = "/transaction_batch")
    public void getTransactionBatch(@RequestParam @Valid @NotNull Long startingIndex, HttpServletResponse response) {
        transactionService.getTransactionBatch(startingIndex, response);
    }

    @GetMapping(value = "/transaction_batch/reactive", produces = MediaType.APPLICATION_STREAM_JSON_VALUE)
    public Flux<byte[]> getTransactionBatchReactive(@RequestParam @Valid @NotNull Long startingIndex) {
        return Flux.create(fluxSink -> transactionService.getTransactionBatch(startingIndex, fluxSink));
    }

    @PostMapping(value = "/transaction/hash")
    public ResponseEntity<IResponse> getSingleTransaction(@RequestBody @Valid GetTransactionRequest getTransactionRequest) {
        return transactionService.getSingleTransaction(getTransactionRequest);
    }
}
