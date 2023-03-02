package io.coti.basenode.controllers;

import io.coti.basenode.data.HealthState;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static io.coti.basenode.services.BaseNodeServiceManager.monitorService;

@Slf4j
@RestController
@RequestMapping("/health")
public class BaseNodeHealthController {


    @Operation(summary = "Get Node Health State", operationId = "getNodeHealthState")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = HealthState.class))})})
    @GetMapping(path = "/total/state")
    public HealthState getNodeHealthState() {
        return monitorService.getLastTotalHealthState();
    }

}
