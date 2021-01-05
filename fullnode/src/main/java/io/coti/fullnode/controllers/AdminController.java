package io.coti.fullnode.controllers;

import io.coti.basenode.http.interfaces.IResponse;
import io.coti.fullnode.http.RepropagateTransactionByAdminRequest;
import io.coti.fullnode.services.TransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private TransactionService transactionService;

    @PostMapping(value = "/transaction/repropagate")
    public ResponseEntity<IResponse> repropagateTransaction(@Valid @RequestBody RepropagateTransactionByAdminRequest repropagateTransactionByAdminRequest) {
        return transactionService.repropagateTransactionByAdmin(repropagateTransactionByAdminRequest);
    }
}
