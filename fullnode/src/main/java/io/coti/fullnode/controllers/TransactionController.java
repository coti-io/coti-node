package io.coti.fullnode.controllers;

import io.coti.basenode.data.TransactionIndexData;
import io.coti.basenode.http.*;
import io.coti.basenode.http.data.ReducedTransactionResponseData;
import io.coti.basenode.http.data.TransactionResponseData;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.fullnode.http.AddTransactionResponse;
import io.coti.fullnode.http.GetAddressTransactionHistoryResponse;
import io.coti.fullnode.http.GetExtendedTransactionResponse;
import io.coti.fullnode.http.GetTotalTransactionsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.Positive;

import static io.coti.fullnode.http.HttpStringConstants.EXPLORER_TRANSACTION_PAGE_INVALID;
import static io.coti.fullnode.services.NodeServiceManager.transactionIndexService;
import static io.coti.fullnode.services.NodeServiceManager.transactionService;

@Slf4j
@RestController
@RequestMapping("/transaction")
@Validated
public class TransactionController {

    @Operation(summary = "Add a New Transaction", operationId = "addNewTransaction")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Transaction created.",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = AddTransactionResponse.class))}),
            @ApiResponse(responseCode = "400", description = "Transaction already exists!",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Transaction authorization failed.",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal error while adding new transaction.",
                    content = @Content)})
    @PutMapping()
    public ResponseEntity<Response> addTransaction(@Valid @RequestBody AddTransactionRequest addTransactionRequest) {
        return transactionService.addNewTransaction(addTransactionRequest);
    }

    @Operation(summary = "Resend Transaction to the Network", operationId = "repropagateTransaction")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transaction resent to the network",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = Response.class))}),
            @ApiResponse(responseCode = "400", description = "Transaction requested to resend is not available in the database",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Invalid transaction resend request signature",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "#1 Transaction is requested to resend not by the transaction sender <br/>"
                    + "#2 Transaction requested to resend is still processed", content = @Content)})
    @PostMapping(value = "/repropagate")
    public ResponseEntity<IResponse> repropagateTransaction(@Valid @RequestBody RepropagateTransactionRequest repropagateTransactionRequest) {
        return transactionService.repropagateTransactionByWallet(repropagateTransactionRequest);
    }

    @Operation(summary = "Get Transaction Details", operationId = "getTransactionDetails")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = GetTransactionResponse.class))}),
            @ApiResponse(responseCode = "400", description = "Transaction doesn't exist",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Transaction details server error",
                    content = @Content)})
    @PostMapping()
    public ResponseEntity<IResponse> getTransactionDetails(@Valid @RequestBody GetTransactionRequest getTransactionRequest) {
        return transactionService.getTransactionDetails(getTransactionRequest.getTransactionHash(), false);
    }

    @Operation(summary = "Get Extended Transaction Details", operationId = "getExtendedTransactionDetails")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = GetExtendedTransactionResponse.class))}),
            @ApiResponse(responseCode = "400", description = "Transaction doesn't exist",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Transaction details server error",
                    content = @Content)})
    @PostMapping(value = "/extended")
    public ResponseEntity<IResponse> getExtendedTransactionDetails(@Valid @RequestBody GetTransactionRequest getTransactionRequest) {
        return transactionService.getTransactionDetails(getTransactionRequest.getTransactionHash(), true);
    }

    @Operation(summary = "Get Multiple Transactions Details", operationId = "getTransactions")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = GetTransactionResponse.class))})})
    @PostMapping(value = "/multiple")
    public void getTransactions(@Valid @RequestBody GetTransactionsRequest getTransactionsRequest, HttpServletResponse response) {
        transactionService.getTransactions(getTransactionsRequest, response);
    }

    @Operation(summary = "Get Transaction History for the Address", operationId = "getAddressTransactions")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = GetAddressTransactionHistoryResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Address transactions server error",
                    content = @Content)})
    @PostMapping(value = "/addressTransactions")
    public ResponseEntity<IResponse> getAddressTransactions(@Valid @RequestBody AddressRequest addressRequest) {
        return transactionService.getAddressTransactions(addressRequest.getAddress());
    }

    @Operation(summary = "Get Transaction History for Multiple Addresses", operationId = "getAddressTransactionBatch")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = TransactionResponseData.class))})})
    @PostMapping(value = "/addressTransactions/batch")
    public void getAddressTransactionBatch(@Valid @RequestBody GetAddressTransactionBatchRequest getAddressTransactionBatchRequest, HttpServletResponse response) {
        transactionService.getAddressTransactionBatch(getAddressTransactionBatchRequest, response, false);
    }

    @Operation(summary = "Get Transaction History for Multiple Addresses by Timestamp", operationId = "getAddressTransactionBatchByTimestamp")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = TransactionResponseData.class))})})
    @PostMapping(value = "/addressTransactions/timestamp/batch")
    public void getAddressTransactionBatchByTimestamp(@Valid @RequestBody GetAddressTransactionBatchByTimestampRequest getAddressTransactionBatchByTimestampRequest, HttpServletResponse response) {
        transactionService.getAddressTransactionBatchByTimestamp(getAddressTransactionBatchByTimestampRequest, response, false);
    }

    @Operation(summary = "Get Transaction History for Multiple Addresses by Date", operationId = "getAddressTransactionBatchByDate")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = TransactionResponseData.class))})})
    @PostMapping(value = "/addressTransactions/date/batch")
    public void getAddressTransactionBatchByDate(@Valid @RequestBody GetAddressTransactionBatchByDateRequest getAddressTransactionBatchByDateRequest, HttpServletResponse response) {
        transactionService.getAddressTransactionBatchByDate(getAddressTransactionBatchByDateRequest, response, false);
    }

    @Operation(summary = "Get Reduced Transaction History for Multiple Addresses", operationId = "getAddressTransactionBatch")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ReducedTransactionResponseData.class))})})
    @PostMapping(value = "/addressTransactions/reduced/batch")
    public void getAddressReducedTransactionBatch(@Valid @RequestBody GetAddressTransactionBatchRequest getAddressTransactionBatchRequest, HttpServletResponse response) {
        transactionService.getAddressTransactionBatch(getAddressTransactionBatchRequest, response, true);
    }

    @PostMapping(value = "/addressTransactions/rejected/batch")
    public void getAddressRejectedTransactionBatch(@Valid @RequestBody GetAddressTransactionBatchRequest getAddressTransactionBatchRequest, HttpServletResponse response) {
        transactionService.getAddressRejectedTransactionBatch(getAddressTransactionBatchRequest, response);
    }

    @Operation(summary = "Get Reduced Transaction History for Multiple Addresses by Timestamp", operationId = "getAddressTransactionBatchByTimestamp")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ReducedTransactionResponseData.class))})})
    @PostMapping(value = "/addressTransactions/timestamp/reduced/batch")
    public void getAddressReducedTransactionBatchByTimestamp(@Valid @RequestBody GetAddressTransactionBatchByTimestampRequest getAddressTransactionBatchByTimestampRequest, HttpServletResponse response) {
        transactionService.getAddressTransactionBatchByTimestamp(getAddressTransactionBatchByTimestampRequest, response, true);
    }

    @Operation(summary = "Get Transaction History for Multiple Addresses by Date", operationId = "getAddressTransactionBatchByDate")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ReducedTransactionResponseData.class))})})
    @PostMapping(value = "/addressTransactions/date/reduced/batch")
    public void getAddressReducedTransactionBatchByDate(@Valid @RequestBody GetAddressTransactionBatchByDateRequest getAddressTransactionBatchByDateRequest, HttpServletResponse response) {
        transactionService.getAddressTransactionBatchByDate(getAddressTransactionBatchByDateRequest, response, true);
    }

    @Operation(summary = "Get Latest Transactions", operationId = "getLastTransactions")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = GetTransactionsResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Address transactions server error",
                    content = @Content)})
    @GetMapping(value = "/lastTransactions")
    public ResponseEntity<IResponse> getLastTransactions() {
        return transactionService.getLastTransactions();
    }

    @Operation(summary = "Get Total Number of Transactions", operationId = "getTotalTransactions")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = GetTotalTransactionsResponse.class))})})
    @GetMapping(value = "/total")
    public ResponseEntity<IResponse> getTotalTransactions() {
        return transactionService.getTotalTransactions();
    }

    @Operation(summary = "Get Transactions by Page", operationId = "getTransactionsByPage")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = GetTransactionsResponse.class))}),
            @ApiResponse(responseCode = "400", description = "Explorer transaction page doesn't exist",
                    content = @Content)})
    @GetMapping()
    public ResponseEntity<IResponse> getTransactionsByPage(@RequestParam @Positive(message = EXPLORER_TRANSACTION_PAGE_INVALID) int page) {
        return transactionService.getTransactionsByPage(page);
    }

    @Operation(summary = "Get Last Transaction Index Data", operationId = "getLastTransactionIndexData")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = TransactionIndexData.class))})})
    @GetMapping(value = "/index")
    public ResponseEntity<TransactionIndexData> getCurrentIndex() {
        return ResponseEntity.ok(transactionIndexService.getLastTransactionIndexData());
    }
}
