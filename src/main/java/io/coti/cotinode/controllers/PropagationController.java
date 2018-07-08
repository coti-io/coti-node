package io.coti.cotinode.controllers;

import io.coti.cotinode.http.*;
import io.coti.cotinode.service.TransactionService;
import io.coti.cotinode.service.interfaces.IPropagationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

@RestController
public class PropagationController {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private IPropagationService propagationService;

    @RequestMapping(value = "/propagatedTransaction", method = POST)
    public ResponseEntity<Response> getTransaction(@Valid @RequestBody GetTransactionRequest getTransactionRequest) {
        return propagationService.getTransaction(getTransactionRequest);
    }

    @RequestMapping(value = "/propagatedTransaction", method = PUT)
    public ResponseEntity<Response> addTransaction(@Valid @RequestBody AddTransactionRequest addTransactionRequest) {
        return transactionService.addTransactionFromPropagation(addTransactionRequest);
    }

    @RequestMapping(value = "/initPropagatedTransaction", method = POST)
    public ResponseEntity<Response> getTransactionsFromCurrentNode(@Valid @RequestBody GetTransactionsRequest getTransactionsRequest) {
        return propagationService.getTransactionsFromCurrentNode(getTransactionsRequest);
    }
}
