package io.coti.basenode.controllers;

import io.coti.basenode.data.Hash;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static io.coti.basenode.services.BaseNodeServiceManager.nodeIdentityService;

@Slf4j
@RestController
@RequestMapping("/nodeHash")
@Tag(name = "node hash")
public class NodeHashController {

    @Operation(summary = "Get Node Hash", operationId = "getNodeHash")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = Hash.class))})})
    @GetMapping()
    public Hash getNodeHash() {
        return nodeIdentityService.getNodeHash();
    }
}