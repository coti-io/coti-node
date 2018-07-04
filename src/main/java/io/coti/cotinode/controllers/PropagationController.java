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
@RequestMapping("/propagatedTransaction")
public class PropagationController {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private IPropagationService propagationService;

    @RequestMapping(method = POST)
    public ResponseEntity<Response> getTransactionFromCurrentNode(@Valid @RequestBody GetTransactionRequest getTransactionRequest) {
        return propagationService.getTransactionFromCurrentNode(getTransactionRequest);
    }

    @RequestMapping(method = PUT)
    public ResponseEntity<AddTransactionResponse> addTransaction(@Valid @RequestBody AddTransactionRequest addTransactionRequest) {
        return transactionService.addTransactionFromPropagation(addTransactionRequest);
    }
}
