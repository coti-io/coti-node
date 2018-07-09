package io.coti.cotinode.controllers;

import io.coti.cotinode.http.*;
import io.coti.cotinode.service.TransactionService;
import io.coti.cotinode.service.interfaces.IPropagationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

@RestController
public class FullNodePropagation {

    @Autowired
    private TransactionService transactionService;

    @RequestMapping(value = "/propagatedTransaction", method = PUT)
    public ResponseEntity<Response> addTransaction(@Valid @RequestBody AddTransactionRequest addTransactionRequest) {
        transactionService.addTransactionToFullNodeFromPropagation(addTransactionRequest.transactionData);
        return new ResponseEntity(HttpStatus.OK);
    }
}
