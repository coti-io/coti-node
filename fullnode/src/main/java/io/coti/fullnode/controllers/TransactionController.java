package io.coti.fullnode.controllers;

import io.coti.common.http.*;
import io.coti.common.services.TransactionHelper;
import io.coti.fullnode.services.TransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

@Slf4j
@RestController
@RequestMapping("/transaction")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;
    @Autowired
    private TransactionHelper transactionHelper;

    @RequestMapping(method = PUT)
    public ResponseEntity<Response> addTransaction(@Valid @RequestBody AddTransactionRequest addTransactionRequest) {
        return transactionService.addNewTransaction(addTransactionRequest);
    }

    @RequestMapping(method = POST)
    public ResponseEntity<BaseResponse> getTransactionDetails(@Valid @RequestBody GetTransactionRequest getTransactionRequest) {
        return transactionHelper.getTransactionDetails(getTransactionRequest.transactionHash);
    }

    @RequestMapping(value = "/addressTransactions", method = POST)
    public ResponseEntity<BaseResponse> getAddressTransactions(@Valid @RequestBody AddressRequest addressRequest) throws Exception {
        return transactionService.getAddressTransactions(addressRequest.getAddress());
    }
}