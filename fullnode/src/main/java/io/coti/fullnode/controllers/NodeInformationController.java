package io.coti.fullnode.controllers;

import io.coti.basenode.http.NodeInformationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static io.coti.fullnode.services.NodeServiceManager.nodeInformationService;

@RestController
@RequestMapping("/nodeinformation")
@Tag(name = "node information")
public class NodeInformationController {

    @Operation(summary = "Get Node IPV4 Address", operationId = "getNodeInfo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = NodeInformationResponse.class))})})
    @GetMapping()
    public ResponseEntity<NodeInformationResponse> getNodeInfo() {
        return ResponseEntity.ok(nodeInformationService.getNodeInformation());
    }
}