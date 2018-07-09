package io.coti.fullnode.controllers;

import io.coti.common.http.AddTransactionRequest;
import io.coti.common.http.GetTransactionRequest;
import io.coti.common.http.GetTransactionsRequest;
import io.coti.common.http.Response;
import io.coti.fullnode.service.TransactionService;
import io.coti.fullnode.service.interfaces.IPropagationService;
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
        return transactionService.addTransactionFromPropagation(addTransactionRequest.transactionData);
    }

    @RequestMapping(value = "/initPropagatedTransaction", method = POST)
    public ResponseEntity<Response> getTransactionsFromCurrentNode(@Valid @RequestBody GetTransactionsRequest getTransactionsRequest) {
        return propagationService.getTransactionsFromCurrentNode(getTransactionsRequest);
    }
}
