package io.coti.basenode.controllers;

import io.coti.basenode.http.*;
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

import static io.coti.basenode.services.BaseNodeServiceManager.currencyService;

@Slf4j
@RestController
@RequestMapping("/currencies")
@Tag(name = "currencies")
public class BaseNodeCurrencyController {

    @Operation(summary = "Get user tokens", operationId = "getUserTokens")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = GetUserTokensResponse.class))}),
            @ApiResponse(responseCode = "400", description = "#1 The system is not supporting multi DAG. <br/>" +
                    "#2 Invalid signature", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content)})
    @PostMapping(path = "/token/user")
    public ResponseEntity<IResponse> getUserTokens(@Valid @RequestBody GetUserTokensRequest getUserTokensRequest) {
        return currencyService.getUserTokens(getUserTokensRequest);
    }

    @Operation(summary = "Get token details by currency hash", operationId = "getTokenDetails")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = GetTokenDetailsResponse.class))}),
            @ApiResponse(responseCode = "400", description = "The system is not supporting multi DAG.",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content)})
    @PostMapping(path = "/token/details")
    public ResponseEntity<IResponse> getTokenDetails(@Valid @RequestBody GetTokenDetailsRequest getTokenDetailsRequest) {
        return currencyService.getTokenDetails(getTokenDetailsRequest);
    }

    @Operation(summary = "Get token details by symbol", operationId = "getTokenSymbolDetails")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = GetTokenDetailsResponse.class))}),
            @ApiResponse(responseCode = "400", description = "The system is not supporting multi DAG.",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content)})
    @PostMapping(path = "/token/symbol/details")
    public ResponseEntity<IResponse> getSymbolDetails(@Valid @RequestBody GetTokenSymbolDetailsRequest getTokenSymbolDetailsRequest) {
        return currencyService.getTokenSymbolDetails(getTokenSymbolDetailsRequest);
    }

    @Operation(summary = "Get token transactions history", operationId = "getTokenHistory")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = GetTokenHistoryResponse.class))}),
            @ApiResponse(responseCode = "400", description = "The system is not supporting multi DAG.",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content)})
    @PostMapping(path = "/token/history")
    public ResponseEntity<IResponse> getTokenHistory(@Valid @RequestBody GetTokenHistoryRequest getTokenHistoryRequest) {
        return currencyService.getTokenHistory(getTokenHistoryRequest);
    }
}
