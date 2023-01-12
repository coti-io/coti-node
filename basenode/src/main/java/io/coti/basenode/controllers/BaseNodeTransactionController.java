package io.coti.basenode.controllers;

import io.coti.basenode.http.interfaces.IResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

import static io.coti.basenode.services.BaseNodeServiceManager.transactionIndexService;
import static io.coti.basenode.services.BaseNodeServiceManager.transactionService;

@Slf4j
@RestController
@RequestMapping("/transaction")
public class BaseNodeTransactionController {

    @GetMapping(path = "/none-indexed")
    public ResponseEntity<IResponse> getNoneIndexedTransactions() {
        return transactionService.getNoneIndexedTransactions();
    }

    @GetMapping(path = "/none-indexed/batch")
    public void getNoneIndexedTransactionBatch(HttpServletResponse response) {
        transactionService.getNoneIndexedTransactionBatch(response, true);
    }

    @GetMapping(path = "/postponed")
    public ResponseEntity<IResponse> getPostponedTransactions() {
        return transactionService.getPostponedTransactions();
    }

    @GetMapping(path = "/lastIndex")
    public ResponseEntity<IResponse> getLastTransactionIndex() {
        return transactionIndexService.getLastTransactionIndex();
    }
}
