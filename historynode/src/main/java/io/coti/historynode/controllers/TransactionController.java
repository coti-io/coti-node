package io.coti.historynode.controllers;

import io.coti.basenode.http.interfaces.IResponse;
import io.coti.historynode.http.GetTransactionsByAddressRequest;
import io.coti.historynode.http.GetTransactionsByDateRequest;
import io.coti.historynode.services.HistoryTransactionService;
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
    private HistoryTransactionService historyTransactionService;

    @PostMapping(path  = "/transactions")
    public ResponseEntity<IResponse> getTransactionsByAddress(@Valid @RequestBody GetTransactionsByAddressRequest getTransactionsByAddressRequest) {
        return historyTransactionService.getTransactionsByAddress(getTransactionsByAddressRequest);
    }

    @PostMapping (path  = "/transactionsByDate")
    public ResponseEntity<IResponse> getTransactionsByDate(@Valid @RequestBody GetTransactionsByDateRequest getTransactionsByDateRequest) {
        return historyTransactionService.getTransactionsByDate(getTransactionsByDateRequest);
    }
}
