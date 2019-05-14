package io.coti.zerospend.controllers;

import io.coti.basenode.http.interfaces.IResponse;
import io.coti.zerospend.http.SetTransactionIndexRequest;
import io.coti.zerospend.services.TransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/admin")
@Slf4j
public class AdminController {

    @Autowired
    private TransactionService transactionService;

    @GetMapping(path = "/transaction/none-indexed")
    public ResponseEntity<IResponse> getNoneIndexedTransactions() {
        return transactionService.getNoneIndexedTransactions();
    }

    @PutMapping(path = "/transaction/index")
    public ResponseEntity<IResponse> setTransactionIndex(@RequestBody @Valid SetTransactionIndexRequest setTransactionIndexRequest) {
        return transactionService.setTransactionIndex(setTransactionIndexRequest);
    }

}
