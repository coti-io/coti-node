package io.coti.financialserver.controllers;

import io.coti.basenode.http.interfaces.IResponse;
import io.coti.financialserver.http.TransactionRequest;
import io.coti.financialserver.services.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/transaction")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @PutMapping(path = "/receiverBaseTransactionOwner")
    public ResponseEntity<IResponse> setReceiverBaseTransactionOwner(@Valid @RequestBody TransactionRequest transactionRequest) {

        return transactionService.setReceiverBaseTransactionOwner(transactionRequest);
    }
}
