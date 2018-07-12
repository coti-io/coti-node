package io.coti.fullnode.controllers;

import io.coti.common.exceptions.TransactionException;
import io.coti.common.http.AddTransactionDataRequest;
import io.coti.common.http.GetLastIndexResponse;
import io.coti.common.services.interfaces.ITransactionService;
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
    private ITransactionService transactionService;

    @RequestMapping(value = "/test", method = POST)
    public ResponseEntity<GetLastIndexResponse> test() {

        ResponseEntity<GetLastIndexResponse> response = ResponseEntity.status(HttpStatus.OK).body(new GetLastIndexResponse(42));
        return response;
    }

    @RequestMapping(value = "/propagatedAddTransaction", method = PUT)
    public void addTransaction(@Valid @RequestBody AddTransactionDataRequest addTransactionDataRequest) {
        try {
            transactionService.addTransactionFromPropagation(addTransactionDataRequest.transactionData);
        } catch (TransactionException e) {
            log.error("Exception in Propagation from DSP server:", e);
        }
    }

    @RequestMapping(value = "/propagatedUpdateTransaction", method = PUT)
    public void setTransactionConfirmedFromPropagation(@Valid @RequestBody AddTransactionDataRequest addTransactionDataRequest) {
        try {
            transactionService.setTransactionConfirmedFromPropagation(addTransactionDataRequest.transactionData);
        } catch (TransactionException e) {
            log.error("Exception in update confirmed propagation from DSP server:", e);
        }
    }
}
