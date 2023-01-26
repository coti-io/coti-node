package io.coti.basenode.controllers;

import io.coti.basenode.http.AddressFileRequest;
import io.coti.basenode.http.GetNodeFeesDataResponse;
import io.coti.basenode.http.NodeFeeSetRequest;
import io.coti.basenode.http.Response;
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

    @Operation(summary = "Set Node Fee", operationId = "setFeeValue")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = GetNodeFeesDataResponse.class))}),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters.",
                    content = @Content)})
    @PostMapping(path = "/fee/set")
    public ResponseEntity<IResponse> setFeeValue(@Valid @RequestBody NodeFeeSetRequest nodeFeeSetRequest) {
        return nodeFeesService.setFeeValue(nodeFeeSetRequest);
    }
}
