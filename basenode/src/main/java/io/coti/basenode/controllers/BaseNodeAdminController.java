package io.coti.basenode.controllers;

import io.coti.basenode.http.*;
import io.coti.basenode.http.data.AddressResponseData;
import io.coti.basenode.http.interfaces.IResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import static io.coti.basenode.services.BaseNodeServiceManager.*;

@Slf4j
@RestController
@RequestMapping("/admin")
@Tag(name = "base administration")
public class BaseNodeAdminController {

    @Operation(summary = "Get Addresses Data from DB", operationId = "getAddressBatch")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = AddressResponseData.class))})})
    @GetMapping(path = "/address/batch")
    public void getAddressBatch(HttpServletResponse response) {
        addressService.getAddressBatch(response);
    }

    @Operation(summary = "Upload Addresses Batch Data", operationId = "uploadAddressBatch")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Address batch uploaded",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = Response.class))}),
            @ApiResponse(responseCode = "500", description = "Address batch upload error",
                    content = @Content)})
    @PostMapping(path = "/address/batch")
    public ResponseEntity<IResponse> uploadAddressBatch(@ModelAttribute @Valid AddressFileRequest request) {
        return addressService.uploadAddressBatch(request);
    }

    @Operation(summary = "Range compaction of database", operationId = "compactRange")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = boolean.class))})})
    @GetMapping(path = "/database/compactRange")
    public boolean compactRange() {
        return databaseService.compactRange();
    }

    @Operation(summary = "Start manual DB backup", operationId = "manualBackupDB")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful manual DB backup",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = Response.class))}),
            @ApiResponse(responseCode = "400", description = "Manual DB backup not allowed",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content)})
    @GetMapping(path = "/database/backup")
    public ResponseEntity<IResponse> backup() {
        return dbRecoveryService.manualBackupDB();
    }

    @Operation(summary = "Set Node Constant Fee", operationId = "setFeeValue")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = GetNodeFeesDataResponse.class))}),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters.",
                    content = @Content)})
    @PostMapping(path = "/fee/set/constant")
    public ResponseEntity<IResponse> setFeeValue(@Valid @RequestBody ConstantTokenFeeSetRequest constantTokenFeeSetRequest) {
        return nodeFeesService.setFeeValue(constantTokenFeeSetRequest);
    }

    @Operation(summary = "Set Node Ratio Fee", operationId = "setFeeValue")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = GetNodeFeesDataResponse.class))}),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters.",
                    content = @Content)})
    @PostMapping(path = "/fee/set/ratio")
    public ResponseEntity<IResponse> setFeeValue(@Valid @RequestBody RatioTokenFeeSetRequest ratioTokenFeeSetRequest) {
        return nodeFeesService.setFeeValue(ratioTokenFeeSetRequest);
    }

    @Operation(summary = "Delete Node Fee", operationId = "deleteFeeValue")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = GetNodeFeesDataResponse.class))}),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters.",
                    content = @Content)})
    @DeleteMapping(path = "/fee/delete")
    public ResponseEntity<IResponse> deleteFeeValue(@Valid @RequestBody DeleteTokenFeeRequest deleteTokenFeeRequest) {
        return nodeFeesService.deleteFeeValue(deleteTokenFeeRequest);
    }

    @Operation(summary = "Retrieve List of Rejected Transactions by Admin", operationId = "getRejectedTransactions")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of Rejected Transactions",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = GetRejectedTransactionsResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Server error while getting rejected transactions",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = Response.class))})})
    @GetMapping(path = "/transaction/rejected")
    public ResponseEntity<IResponse> getRejectedTransactions() {
        return transactionService.getRejectedTransactions();
    }

    @Operation(summary = "Delete Rejected Transactions", operationId = "deleteRejectedTransactions")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of Deleted Rejected Transactions",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = GetRejectedTransactionsResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Server error while deleting rejected transactions",
                    content = @Content)})
    @DeleteMapping(path = "/transaction/rejected/delete")
    public ResponseEntity<IResponse> deleteRejectedTransactions(@Valid @RequestBody DeleteRejectedTransactionsRequest deleteRejectedTransactionsRequest) {
        return transactionService.deleteRejectedTransactions(deleteRejectedTransactionsRequest);
    }
}
