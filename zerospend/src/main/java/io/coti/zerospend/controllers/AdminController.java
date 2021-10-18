package io.coti.zerospend.controllers;

import io.coti.basenode.http.interfaces.IResponse;
import io.coti.zerospend.http.SetIndexesRequest;
import io.coti.zerospend.services.TransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
@Slf4j
public class AdminController {

    @Autowired
    private TransactionService transactionService;

    @PutMapping(path = "/transaction/index")
    public ResponseEntity<IResponse> setIndexToTransactions(@RequestBody SetIndexesRequest setIndexesRequest) {
        return transactionService.setIndexToTransactions(setIndexesRequest);
    }

}
