package io.coti.basenode.controllers;

import io.coti.basenode.http.GetNodeFeesDataResponse;
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

import static io.coti.basenode.services.BaseNodeServiceManager.nodeFeesService;

@Slf4j
@RestController
@RequestMapping("/fee")
@Tag(name = "base fees")
public class BaseNodeFeeController {

    @Operation(summary = "Get Node Fees Data", operationId = "getFeesValues")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = GetNodeFeesDataResponse.class))})})
    @GetMapping()
    public ResponseEntity<IResponse> getFeesValues() {
        return nodeFeesService.getNodeFees();
    }

}
