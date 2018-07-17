package io.coti.dspnode.controllers;

import io.coti.common.data.TransactionData;
import io.coti.common.http.Response;
import io.coti.dspnode.services.TransactionReceiverService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static org.springframework.web.bind.annotation.RequestMethod.PUT;


@Slf4j
@RestController
@RequestMapping("/transaction")
public class TransactionController {
    @Autowired
    private TransactionReceiverService transactionReceiverService;

    @RequestMapping(method = PUT)
    public ResponseEntity<Response> addPropagatedTransactionFromFullNode(@Valid @RequestBody TransactionData transactionData) {
        return transactionReceiverService.addPropagatedTransactionFromFullNode(transactionData);
    }
}