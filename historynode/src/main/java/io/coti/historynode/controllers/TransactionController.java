package io.coti.historynode.controllers;

import io.coti.basenode.http.interfaces.IResponse;
import io.coti.historynode.http.GetTransactionsByAddressRequest;
import io.coti.historynode.http.GetTransactionsByDateRequest;
import io.coti.historynode.services.TransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;


@Slf4j
@RestController
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @PostMapping(path  = "/transactions")
    public ResponseEntity<IResponse> getTransactionsByAddress(@Valid @RequestBody GetTransactionsByAddressRequest getTransactionsByAddressRequest) {
        return transactionService.getTransactionsByAddress(getTransactionsByAddressRequest);
    }

    @PostMapping (path  = "/transactionsByDate")
    public ResponseEntity<IResponse> getTransactionsByDate(@Valid @RequestBody GetTransactionsByDateRequest getTransactionsByDateRequest) {
        return transactionService.getTransactionsByDate(getTransactionsByDateRequest);
    }
}
