package io.coti.fullnode.controllers;

import io.coti.common.http.AddTransactionRequest;
import io.coti.common.http.GetTransactionRequest;
import io.coti.common.http.GetTransactionResponse;
import io.coti.common.http.Response;
import io.coti.fullnode.service.interfaces.ITransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

@Slf4j
@RestController
@RequestMapping("/transaction")
public class TransactionController {

    @Autowired
    private ITransactionService transactionService;

    @RequestMapping(method = PUT)
    public ResponseEntity<Response> addTransaction(@Valid @RequestBody AddTransactionRequest addTransactionRequest) {
        return transactionService.addNewTransaction(addTransactionRequest);
    }

    @RequestMapping(method = POST)
    public ResponseEntity<Response> getTransactionDetails(@Valid @RequestBody GetTransactionRequest getTransactionRequest) {
        return transactionService.getTransactionDetails(getTransactionRequest.transactionHash);
    }

    @RequestMapping(method = GET)
    public ResponseEntity<GetTransactionResponse> getLastTransaction() {
        GetTransactionResponse response = new GetTransactionResponse (transactionService.getLastTransactionHash());
        return new ResponseEntity<> (response, HttpStatus.OK );
    }
}