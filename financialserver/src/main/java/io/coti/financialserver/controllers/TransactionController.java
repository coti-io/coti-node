package io.coti.financialserver.controllers;

import lombok.extern.slf4j.Slf4j;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import io.coti.basenode.http.interfaces.IResponse;
import io.coti.financialserver.http.TransactionRequest;
import io.coti.financialserver.services.TransactionService;

@Slf4j
@RestController
@RequestMapping("/transaction")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<IResponse> newTransaction(@Valid @RequestBody TransactionRequest transactionRequest) {

        return transactionService.newTransaction(transactionRequest);
    }
}
