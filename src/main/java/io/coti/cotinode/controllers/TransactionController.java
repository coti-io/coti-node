package io.coti.cotinode.controllers;

import io.coti.cotinode.data.Hash;
import io.coti.cotinode.data.TransactionData;
import io.coti.cotinode.http.AddAddressRequest;
import io.coti.cotinode.http.AddAddressResponse;
import io.coti.cotinode.service.interfaces.IAddressService;
import io.coti.cotinode.service.interfaces.ITransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

@Controller
@RequestMapping("/transaction")
public class TransactionController {

    @Autowired
    private ITransactionService transactionService;

    @RequestMapping(method = PUT)
    public void addTransaction(@RequestBody TransactionData transactionData) {
        if (transactionData == null) {
            return;
        }
        transactionService.addNewTransaction(transactionData);
    }

    @RequestMapping(method = POST)
    public void getTransactionDetails(@RequestBody Hash transactionHash) {
        if (transactionHash == null) {
            return;
        }
        transactionService.getTransactionData(transactionHash);
    }
}