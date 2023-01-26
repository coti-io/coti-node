package io.coti.fullnode.controllers;

import io.coti.basenode.http.interfaces.IResponse;
import io.coti.fullnode.http.FullNodeFeeRequest;
import io.coti.fullnode.http.FullNodeFeeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static io.coti.fullnode.services.NodeServiceManager.feeService;

@RequestMapping("/fee")
@RestController
@Tag(name = "fees")
public class FeeController {

    @Operation(summary = "Create a Full Node Fee", operationId = "createFullNodeFee")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = FullNodeFeeResponse.class))}),
            @ApiResponse(responseCode = "400", description = "#1 Invalid amount <br/>" +
                    "#2 Transaction amount should be greater than minimum full node fee <br/>" +
                    "#3 Fee rules for this token type are not defined yet <br/>" +
                    "#4 The system is not supporting multi DAG.",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Invalid signature",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content)})
    @PutMapping()
    public ResponseEntity<IResponse> createFullNodeFee(@Valid @RequestBody FullNodeFeeRequest fullNodeFeeRequest) {
        return feeService.createFullNodeFee(fullNodeFeeRequest);
    }
}
