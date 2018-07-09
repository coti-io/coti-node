package io.coti.zero_spend.controllers;

import io.coti.common.data.TransactionData;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/get_transaction")
public class ZeroSpendTransactionController {

    public ResponseEntity<TransactionData> getZeroSpendTransaction(){

        return null;

    }

}
