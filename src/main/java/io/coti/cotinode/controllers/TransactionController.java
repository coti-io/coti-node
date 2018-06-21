package io.coti.cotinode.controllers;

import io.coti.cotinode.data.Hash;
import io.coti.cotinode.http.AddTransactionRequest;
import io.coti.cotinode.http.AddTransactionResponse;
import io.coti.cotinode.http.GetTransactionRequest;
import io.coti.cotinode.http.GetTransactionResponse;
import io.coti.cotinode.service.interfaces.ITransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

@RestController
@RequestMapping("/transaction")
public class TransactionController {

    @Autowired
    private ITransactionService transactionService;

    @RequestMapping(method = PUT)
    @ResponseStatus(HttpStatus.CREATED)
    public AddTransactionResponse addTransaction(@Valid @RequestBody AddTransactionRequest addTransactionRequest) {
        transactionService.addNewTransaction(addTransactionRequest);
        return new AddTransactionResponse(addTransactionRequest.transactionHash);
    }

    @RequestMapping(method = POST)
    public GetTransactionResponse getTransactionDetails(@Valid @RequestBody GetTransactionRequest getTransactionRequest) {
        transactionService.getTransactionData(getTransactionRequest.transactionHash);
        return new GetTransactionResponse();
    }
}