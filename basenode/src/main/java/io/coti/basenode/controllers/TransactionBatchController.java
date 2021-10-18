package io.coti.basenode.controllers;

import io.coti.basenode.services.interfaces.ITransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Slf4j
@RestController
public class TransactionBatchController {

    @Autowired
    private ITransactionService transactionService;

    @GetMapping(value = "/transaction_batch")
    public void getTransactionBatch(@RequestParam(name = "starting_index") @Valid @NotNull Long startingIndex, HttpServletResponse response) {
        transactionService.getTransactionBatch(startingIndex, response);
    }

    @GetMapping(value = "/transaction_batch/reactive", produces = MediaType.APPLICATION_STREAM_JSON_VALUE)
    public Flux<byte[]> getTransactionBatchReactive(@RequestParam(name = "starting_index") @Valid @NotNull Long startingIndex) {
        return Flux.create(fluxSink -> transactionService.getTransactionBatch(startingIndex, fluxSink));
    }
}
