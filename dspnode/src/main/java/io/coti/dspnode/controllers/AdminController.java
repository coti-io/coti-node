package io.coti.dspnode.controllers;

import io.coti.basenode.http.interfaces.IResponse;
import io.coti.dspnode.services.TransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private TransactionService transactionService;


    @GetMapping(path = "/transaction/rejected")
    public ResponseEntity<IResponse> getRejectedTransactions() {
        return transactionService.getRejectedTransactions();
    }
}
