package io.coti.fullnode.controllers;

import io.coti.basenode.services.liveview.data.GraphData;
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

import static io.coti.fullnode.services.NodeServiceManager.liveViewService;

@RequestMapping("/fullgraph")
@RestController
@Slf4j
@Tag(name = "graph")
public class GraphController {

    @Operation(summary = "Get Graph Data", operationId = "getFullGraph")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = GraphData.class))})})
    @GetMapping()
    public GraphData getFullGraph() {
        return liveViewService.getFullGraph();
    }
}
