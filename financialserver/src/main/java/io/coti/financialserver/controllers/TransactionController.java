package io.coti.financialserver.controllers;

import io.coti.basenode.http.interfaces.IResponse;
import io.coti.financialserver.http.TransactionRequest;
import io.coti.financialserver.services.TransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
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
