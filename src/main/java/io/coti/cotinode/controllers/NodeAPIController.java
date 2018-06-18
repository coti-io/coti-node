package io.coti.cotinode.controllers;

import io.coti.cotinode.data.Hash;
import io.coti.cotinode.data.NodeInformation;
import io.coti.cotinode.data.TransactionData;
import io.coti.cotinode.http.GetBalancesRequest;
import io.coti.cotinode.http.GetBalancesResponse;
import io.coti.cotinode.service.BalanceService;
import io.coti.cotinode.service.NodeInformationService;
import io.coti.cotinode.service.TransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.web.bind.annotation.RequestMethod.*;

@Slf4j
@RestController
public class NodeAPIController {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private BalanceService balanceService;

    @Autowired
    private NodeInformationService nodeInformationService;

    @RequestMapping(value = "/transaction", method = PUT)
    public void addTransaction(@RequestBody TransactionData transactionData) {
        if (transactionData == null) {
            return;
        }
        transactionService.addNewTransaction(transactionData);
    }

    @RequestMapping(value = "/transaction", method = POST)
    public void getTransactionDetails(@RequestBody Hash transactionHash) {
        if (transactionHash == null) {
            return;
        }
        transactionService.getTransactionData(transactionHash);
    }

    @RequestMapping(value = "/nodeInfo", method = GET)
    public NodeInformation getNodeInfo() {
        return nodeInformationService.getNodeInformation();
    }

//    @RequestMapping(value = "/address", method = PUT)
//    public boolean addAddress(@RequestBody Hash addressHash) {
//        if (addressHash == null) {
//            return false;
//        }
//        return balanceService.addNewAddress(addressHash);
//    }
}
