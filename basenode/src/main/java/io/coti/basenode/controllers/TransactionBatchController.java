package io.coti.basenode.controllers;

import io.coti.basenode.http.GetTransactionBatchResponse;
import io.coti.basenode.services.TransactionHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class TransactionBatchController {

    @Autowired
    private TransactionHelper transactionHelper;


    @GetMapping(value = "/transaction_batch")
    public ResponseEntity<GetTransactionBatchResponse> getTransactionBatch(@RequestParam long starting_index) {
        return ResponseEntity.ok(transactionHelper.getTransactionBatch(starting_index));
    }
}
