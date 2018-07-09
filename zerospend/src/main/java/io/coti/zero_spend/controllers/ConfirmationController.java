package io.coti.zero_spend.controllers;


import io.coti.common.data.TransactionData;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/confirm")
public class ConfirmationController {


    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<String> confirmTransaction(TransactionData transactionData) {


        return null;
    }

}
