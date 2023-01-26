package io.coti.basenode.controllers;

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

import static io.coti.basenode.services.BaseNodeServiceManager.dbRecoveryService;

@Slf4j
@RestController
@RequestMapping("/backup")
@Tag(name = "backups")
public class BackupController {

    @Operation(summary = "Get backup AWS bucket name", operationId = "getBackupBucket")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Backup enabled",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseEntity.class))}),
            @ApiResponse(responseCode = "401", description = "Backup not enabled.",
                    content = @Content)})
    @GetMapping(path = "/bucket")
    public ResponseEntity<IResponse> getBackupBucket() {
        return dbRecoveryService.getBackupBucket();
    }

}
