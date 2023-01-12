package io.coti.dspnode.controllers;

import io.coti.basenode.http.interfaces.IResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static io.coti.dspnode.services.NodeServiceManager.transactionService;

@Slf4j
@RestController
@RequestMapping("/admin")
public class AdminController {

    @GetMapping(path = "/transaction/rejected")
    public ResponseEntity<IResponse> getRejectedTransactions() {
        return transactionService.getRejectedTransactions();
    }
}
