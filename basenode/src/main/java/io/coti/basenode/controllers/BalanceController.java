package io.coti.basenode.controllers;

import io.coti.basenode.http.GetBalancesRequest;
import io.coti.basenode.http.GetBalancesResponse;
import io.coti.basenode.http.GetTokenBalancesRequest;
import io.coti.basenode.http.GetTokenBalancesResponse;
import io.coti.basenode.http.interfaces.IResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static io.coti.basenode.services.BaseNodeServiceManager.balanceService;

@RestController
@RequestMapping("/balance")
@Tag(name = "balance")
public class BalanceController {

    @Operation(summary = "List the balance of native COTI for each address", operationId = "getBalances")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Balance for each address",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = GetBalancesResponse.class))})})
    @PostMapping()
    public ResponseEntity<GetBalancesResponse> getBalances(@Valid @RequestBody GetBalancesRequest getBalancesRequest) {
        return balanceService.getBalances(getBalancesRequest);
    }

    @Operation(summary = "List the balance of custom token for each address", operationId = "getTokenBalances")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Balance for each address",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = GetTokenBalancesResponse.class))})})
    @PostMapping(value = "/tokens")
    public ResponseEntity<IResponse> getTokenBalances(@Valid @RequestBody GetTokenBalancesRequest getTokenBalancesRequest) {
        return balanceService.getTokenBalances(getTokenBalancesRequest);
    }
}
