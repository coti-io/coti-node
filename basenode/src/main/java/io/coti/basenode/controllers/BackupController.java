package io.coti.basenode.controllers;

import io.coti.basenode.http.interfaces.IResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static io.coti.basenode.services.BaseNodeServiceManager.dbRecoveryService;

@Slf4j
@RestController
@RequestMapping("/backup")
public class BackupController {

    @GetMapping(path = "/bucket")
    public ResponseEntity<IResponse> getBackupBucket() {
        return dbRecoveryService.getBackupBucket();
    }

}
