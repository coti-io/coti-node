package io.coti.zerospend.controllers;


import io.coti.common.http.AddTransactionRequest;
import io.coti.zerospend.services.ZeroSpendTrxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
public class GetZsTransactionTEST {

    @Autowired
    private ZeroSpendTrxService zeroSpendService;

    @RequestMapping("/zs")
    public String testGetZeroSpendTransaction(@Valid @RequestBody AddTransactionRequest addTransactionRequest) {

        zeroSpendService.publicReceiveZeroSpendTransaction(addTransactionRequest.transactionData);
        return "Good";

    }
}
