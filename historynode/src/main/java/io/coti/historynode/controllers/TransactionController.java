package io.coti.historynode.controllers;

import io.coti.historynode.http.GetTransactionsByAddressRequest;
import io.coti.historynode.http.GetTransactionsByDateRequest;
import io.coti.historynode.services.TransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;


@Slf4j
@RestController
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @PostMapping(path = "/transactions")
    public void getTransactionsByAddress(@Valid @RequestBody GetTransactionsByAddressRequest getTransactionsByAddressRequest, HttpServletResponse response) {
        transactionService.getTransactionsByAddress(getTransactionsByAddressRequest, response);
    }

    @PostMapping(path = "/transactionsByDate")
    public void getTransactionsByDate(@Valid @RequestBody GetTransactionsByDateRequest getTransactionsByDateRequest, HttpServletResponse response) {
        transactionService.getTransactionsByDate(getTransactionsByDateRequest, response);
    }
}
