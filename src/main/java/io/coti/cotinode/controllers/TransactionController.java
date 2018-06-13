package io.coti.cotinode.controllers;

import io.coti.cotinode.model.Transaction;
import io.coti.cotinode.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @RequestMapping("/transaction")
    public Transaction getTransaction(@RequestHeader("Hash") byte[] hash){
        return transactionService.getTransaction(hash);
    }
}
