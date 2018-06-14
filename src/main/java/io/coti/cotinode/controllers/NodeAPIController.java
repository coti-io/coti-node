package io.coti.cotinode.controllers;

import io.coti.cotinode.model.NodeInformation;
import io.coti.cotinode.model.TransactionPackage;
import io.coti.cotinode.service.BalanceService;
import io.coti.cotinode.service.NodeInformationService;
import io.coti.cotinode.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
public class NodeAPIController {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private BalanceService balanceService;

    @Autowired
    private NodeInformationService nodeInformationService;

    @RequestMapping(value = "/transaction", method = POST)
    public void addTransaction(@RequestBody TransactionPackage transactionPackage){
        System.out.println(transactionPackage);
        transactionService.addNewTransaction(transactionPackage);
    }

    @RequestMapping(value = "/nodeInfo", method = GET)
    public NodeInformation getNodeInfo(){
        return nodeInformationService.getNodeInformation();
    }

    @RequestMapping(value = "/balance", method = GET)
    public double getBalance(@RequestHeader("Hash") byte[] addressHash){
        return balanceService.getBalance(addressHash);
    }

    @RequestMapping(value = "/balances", method = GET)
    public List<Double> getBalance(@RequestHeader("Hash")List<byte[]> addressHashes){
        return balanceService.getBalances(addressHashes);
    }

}
