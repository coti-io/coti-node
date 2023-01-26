package io.coti.basenode.controllers;

import io.coti.basenode.data.Event;
import io.coti.basenode.http.GetTransactionResponse;
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

import static io.coti.basenode.services.BaseNodeServiceManager.nodeEventService;

@Slf4j
@RestController
@RequestMapping("/event")
@Tag(name = "events")
public class BaseNodeEventController {

    @Operation(summary = "Get confirmed transaction data for event MULTI DAG", operationId = "getConfirmedMultiDagEvent")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = GetTransactionResponse.class))}),
            @ApiResponse(responseCode = "400", description = "Event Not Found",
                    content = @Content)})
    @GetMapping(path = "/multi-dag/confirmed")
    public ResponseEntity<IResponse> getConfirmedMultiDagEvent() {
        return nodeEventService.getConfirmedEventTransactionDataResponse(Event.MULTI_DAG);
    }

    @Operation(summary = "Get transaction data for event MULTI DAG", operationId = "getMultiDagEventTransactionData")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = GetTransactionResponse.class))}),
            @ApiResponse(responseCode = "400", description = "Event Not Found",
                    content = @Content)})
    @GetMapping(path = "/multi-dag")
    public ResponseEntity<IResponse> getMultiDagEventTransactionData() {
        return nodeEventService.getEventTransactionDataResponse(Event.MULTI_DAG);
    }

    @GetMapping(path = "/trust-score-consensus/confirmed")
    public ResponseEntity<IResponse> getConfirmedTrustScoreConsensusEvent() {
        return nodeEventService.getConfirmedEventTransactionDataResponse(Event.TRUST_SCORE_CONSENSUS);
    }

    @GetMapping(path = "/trust-score-consensus")
    public ResponseEntity<IResponse> getTrustScoreConsensusTransactionData() {
        return nodeEventService.getEventTransactionDataResponse(Event.TRUST_SCORE_CONSENSUS);
    }
}
