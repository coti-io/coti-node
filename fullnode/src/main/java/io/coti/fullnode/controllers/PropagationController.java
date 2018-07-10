package io.coti.fullnode.controllers;

import io.coti.common.http.*;
import io.coti.fullnode.service.TransactionService;
import io.coti.fullnode.service.interfaces.IPropagationService;
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
public class PropagationController {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private IPropagationService propagationService;

    @RequestMapping(value = "/propagatedTransaction", method = PUT)
    public ResponseEntity<Response> addTransaction(@Valid @RequestBody AddTransactionDataRequest addTransactionDataRequest) {
        transactionService.addTransactionFromPropagation(addTransactionDataRequest.transactionData);
        return new ResponseEntity(HttpStatus.OK);
    }

}
