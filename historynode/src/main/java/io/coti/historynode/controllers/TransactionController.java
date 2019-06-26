package io.coti.historynode.controllers;

import io.coti.basenode.http.interfaces.IResponse;
import io.coti.historynode.http.GetTransactionsByAddressRequest;
import io.coti.historynode.http.GetTransactionsRequest;
import io.coti.historynode.services.HistoryTransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@Slf4j
@RestController
public class TransactionController {

    @Autowired
    private HistoryTransactionService historyTransactionService;

    //TODO: old implementation
    @RequestMapping(value = "/transactions", method = GET)
    public ResponseEntity<IResponse> getTransactionsDetails(@Valid @RequestBody GetTransactionsRequest getTransactionRequest) {
        return historyTransactionService.getTransactionsDetails(getTransactionRequest);
    }

    @GetMapping(path  = "/transactionsByAddress")
    public ResponseEntity<IResponse> getTransaction(@Valid @RequestBody GetTransactionsByAddressRequest getTransactionsByAddressRequest) {
        return historyTransactionService.getTransactionsByAddress(getTransactionsByAddressRequest);
    }
}
