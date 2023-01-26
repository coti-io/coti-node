package io.coti.basenode.controllers;

import io.coti.basenode.http.GetExtendedTransactionsResponse;
import io.coti.basenode.http.GetLastTransactionIndexResponse;
import io.coti.basenode.http.GetTransactionsResponse;
import io.coti.basenode.http.interfaces.IResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

import static io.coti.basenode.services.BaseNodeServiceManager.transactionIndexService;
import static io.coti.basenode.services.BaseNodeServiceManager.transactionService;

@Slf4j
@RestController
@RequestMapping("/transaction")
@Tag(name = "base transactions")
public class BaseNodeTransactionController {

    @Operation(summary = "Get none-indexed transactions", operationId = "getNoneIndexedTransactions")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = GetTransactionsResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Server error while getting none indexed transactions",
                    content = @Content)})
    @GetMapping(path = "/none-indexed")
    public ResponseEntity<IResponse> getNoneIndexedTransactions() {
        return transactionService.getNoneIndexedTransactions();
    }

    @Operation(summary = "Get none-indexed transactions extended data", operationId = "getNoneIndexedTransactionBatch")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = GetExtendedTransactionsResponse.class))})})
    @GetMapping(path = "/none-indexed/batch")
    public void getNoneIndexedTransactionBatch(HttpServletResponse response) {
        transactionService.getNoneIndexedTransactionBatch(response, true);
    }

    @Operation(summary = "Get postponed transactions", operationId = "getPostponedTransactions")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = GetExtendedTransactionsResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Server error while getting postponed transactions",
                    content = @Content)})
    @GetMapping(path = "/postponed")
    public ResponseEntity<IResponse> getPostponedTransactions() {
        return transactionService.getPostponedTransactions();
    }

    @Operation(summary = "Get last transaction index", operationId = "getLastTransactionIndex")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = GetLastTransactionIndexResponse.class))})})
    @GetMapping(path = "/lastIndex")
    public ResponseEntity<IResponse> getLastTransactionIndex() {
        return transactionIndexService.getLastTransactionIndex();
    }
}
