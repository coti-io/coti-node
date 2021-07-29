package io.coti.basenode.controllers;

import io.coti.basenode.http.AddressFileRequest;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.services.interfaces.IAddressService;
import io.coti.basenode.services.interfaces.IDBRecoveryService;
import io.coti.basenode.services.interfaces.IDatabaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/admin")
public class BaseNodeAdminController {

    @Autowired
    private IAddressService addressService;

    @Autowired
    private IDatabaseService databaseService;

    @Autowired
    private IDBRecoveryService dbRecoveryService;

    @GetMapping(path = "/address/batch")
    public void getAddressBatch(HttpServletResponse response) {
        addressService.getAddressBatch(response);
    }

    @PostMapping(path = "/address/batch")
    public ResponseEntity<IResponse> uploadAddressBatch(@ModelAttribute @Valid AddressFileRequest request) {
        return addressService.uploadAddressBatch(request);
    }

    @GetMapping(path = "/database/compactRange")
    public boolean compactRange() {
        return databaseService.compactRange();
    }

    @GetMapping(path = "/database/backup")
    public ResponseEntity<Boolean> backup() {
        return dbRecoveryService.backupDB();
    }

}
