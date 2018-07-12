package io.coti.zero_spend.controllers;

import io.coti.zero_spend.http.AddTransactionRequest;
import io.coti.zero_spend.services.interfaces.IAddTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/addTransaction")
public class AddTransactionController {

    @Autowired
    private IAddTransactionService addTransactionService;


    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<String> addTransaction(AddTransactionRequest addTransactionRequest){
        addTransactionService.addTransaction(addTransactionRequest);

        return ResponseEntity.status(HttpStatus.OK).body("Transaction was added successfully");
    }

}
