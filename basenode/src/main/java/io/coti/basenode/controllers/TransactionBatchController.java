package io.coti.basenode.controllers;

import io.coti.basenode.data.TransactionData;
import io.coti.basenode.http.GetTransactionByIndexRequest;
import io.coti.basenode.http.data.TransactionResponseData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "transactions batch")
public class TransactionBatchController {

    @Operation(summary = "Get transaction batch by start and end indexes", operationId = "getTransactionBatchByStartAndEnd")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = TransactionResponseData.class))}),
            @ApiResponse(responseCode = "400", description = "Invalid transaction index!",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Transaction response error",
                    content = @Content)})
    @PostMapping()
    public void getTransactionBatchByStartAndEnd(@RequestBody @Valid GetTransactionByIndexRequest transactionByIndexRequest, HttpServletResponse response) {
        transactionService.getTransactionBatch(transactionByIndexRequest.getStartingIndex(), transactionByIndexRequest.getEndingIndex(), response, transactionByIndexRequest.isExtended(), transactionByIndexRequest.isIncludeRuntimeTrustScore());
    }

    @Operation(summary = "Get transaction batch by start index", operationId = "getTransactionBatch")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = TransactionData.class))})})
    @GetMapping()
    public void getTransactionBatch(@RequestParam(name = "starting_index") @Valid @NotNull Long startingIndex, HttpServletResponse response) {
        transactionService.getTransactionBatch(startingIndex, response);
    }

    @Operation(summary = "Get reactive transaction batch by start index", operationId = "getTransactionBatchReactive")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = Flux.class))})})
    @GetMapping(value = "/reactive", produces = MediaType.APPLICATION_STREAM_JSON_VALUE)
    public Flux<byte[]> getTransactionBatchReactive(@RequestParam(name = "starting_index") @Valid @NotNull Long startingIndex) {
        return Flux.create(fluxSink -> transactionService.getTransactionBatch(startingIndex, fluxSink));
    }
}
