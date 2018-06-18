package io.coti.cotinode.controllers;

import io.coti.cotinode.data.BaseTransactionData;
import io.coti.cotinode.data.Hash;
import io.coti.cotinode.data.NodeInformation;
import io.coti.cotinode.data.TransactionData;
import io.coti.cotinode.service.BalanceService;
import io.coti.cotinode.service.NodeInformationService;
import io.coti.cotinode.service.TransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

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
    public void addTransaction(@RequestBody TransactionData transactionData){
        log.info(transactionData.toString());
        if(transactionData == null){
            return;
        }
        transactionService.addNewTransaction(transactionData);
    }

    @RequestMapping(value = "/nodeInfo", method = GET)
    public NodeInformation getNodeInfo(){
        return nodeInformationService.getNodeInformation();
    }

    @RequestMapping(value = "/balances", method = GET)
    public List<BaseTransactionData> getBalance(@RequestHeader("Hash")List<Hash> addressHashes){
        if(addressHashes == null){
            return new ArrayList<>();
        }
        return balanceService.getBalances(addressHashes);
    }
}
