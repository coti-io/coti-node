package io.coti.fullnode.controllers;

import io.coti.common.http.AddTransactionRequest;
import io.coti.common.http.Response;
import io.coti.fullnode.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static org.springframework.web.bind.annotation.RequestMethod.PUT;

@RestController
public class FullNodePropagationController {

    @Autowired
    private TransactionService transactionService;

    @RequestMapping(value = "/propagatedTransaction", method = PUT)
    public ResponseEntity<Response> addTransaction(@Valid @RequestBody AddTransactionRequest addTransactionRequest) {
        transactionService.addTransactionToFullNodeFromPropagation(addTransactionRequest.transactionData);
        return new ResponseEntity(HttpStatus.OK);
    }
}
