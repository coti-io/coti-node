package io.coti.financialserver.controllers;

import io.coti.basenode.http.TransactionRequest;
import io.coti.basenode.http.interfaces.IResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static io.coti.financialserver.services.NodeServiceManager.transactionService;

@RestController
@RequestMapping("/transaction")
public class TransactionController {

    @PutMapping(path = "/receiverBaseTransactionOwner")
    public ResponseEntity<IResponse> setReceiverBaseTransactionOwner(@Valid @RequestBody TransactionRequest transactionRequest) {

        return transactionService.setReceiverBaseTransactionOwner(transactionRequest);
    }
}
