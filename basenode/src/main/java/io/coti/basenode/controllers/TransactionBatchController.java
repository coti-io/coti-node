package io.coti.basenode.controllers;

import io.coti.basenode.http.GetTransactionByIndexRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import static io.coti.basenode.services.BaseNodeServiceManager.transactionService;

@Slf4j
@RestController
@RequestMapping("/transaction_batch")
public class TransactionBatchController {

    @PostMapping()
    public void getTransactionBatchByStartAndEnd(@RequestBody @Valid GetTransactionByIndexRequest transactionByIndexRequest, HttpServletResponse response) {
        transactionService.getTransactionBatch(transactionByIndexRequest.getStartingIndex(), transactionByIndexRequest.getEndingIndex(), response, transactionByIndexRequest.isExtended(), transactionByIndexRequest.isIncludeRuntimeTrustScore());
    }

    @GetMapping()
    public void getTransactionBatch(@RequestParam(name = "starting_index") @Valid @NotNull Long startingIndex, HttpServletResponse response) {
        transactionService.getTransactionBatch(startingIndex, response);
    }

    @GetMapping(value = "/reactive", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<byte[]> getTransactionBatchReactive(@RequestParam(name = "starting_index") @Valid @NotNull Long startingIndex) {
        return Flux.create(fluxSink -> transactionService.getTransactionBatch(startingIndex, fluxSink));
    }
}
