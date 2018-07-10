package io.coti.fullnode.controllers;

import io.coti.common.http.*;
import io.coti.fullnode.exception.TransactionException;
import io.coti.fullnode.service.TransactionService;
import io.coti.fullnode.service.interfaces.IPropagationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

@Slf4j
@RestController
public class PropagationController {

    @Autowired
    private TransactionService transactionService;

    @RequestMapping(value = "/propagatedAddTransaction", method = PUT)
    public ResponseEntity<Response> addTransaction(@Valid @RequestBody AddTransactionDataRequest addTransactionDataRequest) {
        try {
            transactionService.addTransactionFromPropagation(addTransactionDataRequest.transactionData);
        } catch (TransactionException e) {
            log.error("Exception in Propagation from DSP server:",e);
            return new ResponseEntity(HttpStatus.EXPECTATION_FAILED);
        }
        return new ResponseEntity(HttpStatus.OK);
    }

    @RequestMapping(value = "/propagatedUpdateTransaction", method = PUT)
    public ResponseEntity<Response> setTransactionConfirmedFromPropagation(@Valid @RequestBody AddTransactionDataRequest addTransactionDataRequest) {
        try {
            transactionService.setTransactionConfirmedFromPropagation(addTransactionDataRequest.transactionData);
        } catch (TransactionException e) {
            log.error("Exception in update confirmed propagation from DSP server:",e);
            return new ResponseEntity(HttpStatus.EXPECTATION_FAILED);
        }
        return new ResponseEntity(HttpStatus.OK);
    }
}
