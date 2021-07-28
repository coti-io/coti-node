package io.coti.basenode.controllers;

import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.services.interfaces.IDBRecoveryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/backup")
public class BackupController {

    @Autowired
    private IDBRecoveryService dbRecoveryService;

    @GetMapping(path = "/bucket")
    public ResponseEntity<IResponse> getBackupBucket() {
        return dbRecoveryService.getBackupBucket();
    }

    @GetMapping(path = "/now")
    public boolean backup() { return dbRecoveryService.backupDB(); }

}
