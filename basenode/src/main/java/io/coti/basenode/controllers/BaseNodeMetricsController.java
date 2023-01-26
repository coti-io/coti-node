package io.coti.basenode.controllers;

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

import javax.servlet.http.HttpServletRequest;

import static io.coti.basenode.services.BaseNodeServiceManager.scraperService;

@Slf4j
@RestController
@RequestMapping("/metrics")
@Tag(name = "metrics")
public class BaseNodeMetricsController {

    @Operation(summary = "Get Metrics", operationId = "getMetrics")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class))})})
    @GetMapping()
    public String metrics(HttpServletRequest request) {
        return scraperService.getMetrics(request);
    }

}
