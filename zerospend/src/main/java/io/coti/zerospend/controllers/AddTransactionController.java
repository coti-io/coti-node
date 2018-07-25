package io.coti.zerospend.controllers;

import io.coti.common.http.AddTransactionZeroSpendRequest;
import io.coti.common.http.AddTransactionZeroSpendResponse;
import io.coti.common.services.interfaces.IAddZeroSpendTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/addTransaction")
public class AddTransactionController {

    @Autowired
    private IAddZeroSpendTransactionService addZeroSpendTransactionService;


    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<AddTransactionZeroSpendResponse> addTransaction(AddTransactionZeroSpendRequest addTransactionRequest) {
        return addZeroSpendTransactionService.addTransaction(addTransactionRequest);
    }

}
