package io.coti.zerospend.controllers;


import io.coti.common.data.Hash;
import io.coti.common.data.TransactionData;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;


@RestController
@RequestMapping("/getGenesisTransactions")
public class GetGenesisTransactionsController {

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<TransactionData>> getGenesisTransactions() {
        int currentHashCounter = 0;
        List<TransactionData> genesisTransactions = new LinkedList<>();
        for (int trustScore = 0; trustScore <= 100; trustScore = trustScore + 10) {
            TransactionData transactionData = new TransactionData(new ArrayList<>(),new Hash(currentHashCounter++),
                    "genesis",trustScore, new Date());
            transactionData.setZeroSpend(true);
            transactionData.setSenderTrustScore(80);
            genesisTransactions.add(transactionData);
        }

        return ResponseEntity.status(HttpStatus.OK).body(genesisTransactions);
    }
}
