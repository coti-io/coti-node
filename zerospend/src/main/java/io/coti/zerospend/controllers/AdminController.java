package io.coti.zerospend.controllers;

import io.coti.basenode.http.interfaces.IResponse;
import io.coti.zerospend.http.SetIndexesRequest;
import io.coti.zerospend.services.ClusterStampService;
import io.coti.zerospend.services.TransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@Slf4j
public class AdminController {

    @Autowired
    private TransactionService transactionService;
    @Autowired
    private ClusterStampService clusterStampService;

    @GetMapping(path = "/transaction/none-indexed")
    public ResponseEntity<IResponse> getNoneIndexedTransactions() {
        return transactionService.getNoneIndexedTransactions();
    }

    @PostMapping(path = "/clusterstamp/initiate")
    public ResponseEntity<IResponse> initiateClusterStamp() {
        return clusterStampService.initiateClusterStamp();
    }

    @PutMapping(path = "/transaction/index")
    public ResponseEntity<IResponse> setIndexToTransactions(@RequestBody SetIndexesRequest setIndexesRequest) {
        return transactionService.setIndexToTransactions(setIndexesRequest);
    }

}
