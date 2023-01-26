package io.coti.fullnode.controllers;

import io.coti.basenode.http.RepropagateTransactionByAdminRequest;
import io.coti.basenode.http.Response;
import io.coti.basenode.http.interfaces.IResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static io.coti.fullnode.services.NodeServiceManager.transactionService;

@Slf4j
@RestController
@RequestMapping("/admin")
@Tag(name = "administration")
public class AdminController {

    @Operation(summary = "Resend Transaction by Admin", operationId = "repropagateTransaction")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transaction resent to the network",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = Response.class))}),
            @ApiResponse(responseCode = "400", description = "Transaction requested to resend is not available in the database",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = Response.class))}),
            @ApiResponse(responseCode = "403", description = "Transaction requested to resend is still processed",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = Response.class))})})
    @PostMapping(value = "/transaction/repropagate")
    public ResponseEntity<IResponse> repropagateTransaction(@Valid @RequestBody RepropagateTransactionByAdminRequest repropagateTransactionByAdminRequest) {
        return transactionService.repropagateTransactionByAdmin(repropagateTransactionByAdminRequest);
    }
}
