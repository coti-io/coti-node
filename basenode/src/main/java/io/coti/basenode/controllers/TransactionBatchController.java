package io.coti.basenode.controllers;

import io.coti.basenode.http.GetTransactionByIndexRequest;
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
@RequestMapping("/transaction_batch")
public class TransactionBatchController {

    @Autowired
    private ITransactionService transactionService;

    @PostMapping()
    public ResponseEntity<IResponse> getTransactionBatchByStartAndEnd(@RequestBody @Valid GetTransactionByIndexRequest transactionByIndexRequest, HttpServletResponse response) {
        return transactionService.getTransactionBatch(transactionByIndexRequest.getStartingIndex(), transactionByIndexRequest.getEndingIndex(), response);
    }

    @GetMapping()
    public void getTransactionBatch(@RequestParam(name = "starting_index") @Valid @NotNull Long startingIndex, HttpServletResponse response) {
        transactionService.getTransactionBatch(startingIndex, response);
    }

    @GetMapping(value = "/reactive", produces = MediaType.APPLICATION_STREAM_JSON_VALUE)
    public Flux<byte[]> getTransactionBatchReactive(@RequestParam(name = "starting_index") @Valid @NotNull Long startingIndex) {
        return Flux.create(fluxSink -> transactionService.getTransactionBatch(startingIndex, fluxSink));
    }
}
