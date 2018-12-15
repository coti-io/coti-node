package io.coti.financialserver.controllers;

import io.coti.basenode.http.interfaces.IResponse;
import io.coti.financialserver.http.TransactionRequest;
import io.coti.financialserver.services.TransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/transaction")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @RequestMapping(path = "/receiverBaseTransactionOwner", method = RequestMethod.PUT)
    public ResponseEntity<IResponse> setReceiverBaseTransactionOwner(@Valid @RequestBody TransactionRequest transactionRequest) {

        return transactionService.setReceiverBaseTransactionOwner(transactionRequest);
    }
}
