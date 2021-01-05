package io.coti.fullnode.controllers;

import io.coti.basenode.data.TransactionIndexData;
import io.coti.basenode.http.Response;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.services.TransactionIndexService;
import io.coti.fullnode.http.*;
import io.coti.fullnode.services.TransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.Positive;

import static io.coti.fullnode.http.HttpStringConstants.EXPLORER_TRANSACTION_PAGE_INVALID;

@Slf4j
@RestController
@RequestMapping("/transaction")
@Validated
public class TransactionController {

    @Autowired
    private TransactionService transactionService;
    @Autowired
    private TransactionIndexService transactionIndexService;

    @PutMapping()
    public ResponseEntity<Response> addTransaction(@Valid @RequestBody AddTransactionRequest addTransactionRequest) {
        return transactionService.addNewTransaction(addTransactionRequest);
    }

    @PostMapping(value = "/repropagate")
    public ResponseEntity<IResponse> repropagateTransaction(@Valid @RequestBody RepropagateTransactionRequest repropagateTransactionRequest) {
        return transactionService.repropagateTransactionByWallet(repropagateTransactionRequest);
    }

    @PostMapping()
    public ResponseEntity<IResponse> getTransactionDetails(@Valid @RequestBody GetTransactionRequest getTransactionRequest) {
        return transactionService.getTransactionDetails(getTransactionRequest.getTransactionHash());
    }

    @PostMapping(value = "/multiple")
    public void getTransactions(@Valid @RequestBody GetTransactionsRequest getTransactionsRequest, HttpServletResponse response) {
        transactionService.getTransactions(getTransactionsRequest, response);
    }

    @PostMapping(value = "/addressTransactions")
    public ResponseEntity<IResponse> getAddressTransactions(@Valid @RequestBody AddressRequest addressRequest) {
        return transactionService.getAddressTransactions(addressRequest.getAddress());
    }

    @PostMapping(value = "/addressTransactions/batch")
    public void getAddressTransactionBatch(@Valid @RequestBody GetAddressTransactionBatchRequest getAddressTransactionBatchRequest, HttpServletResponse response) {
        transactionService.getAddressTransactionBatch(getAddressTransactionBatchRequest, response, false);
    }

    @PostMapping(value = "/addressTransactions/reduced/batch")
    public void getAddressReducedTransactionBatch(@Valid @RequestBody GetAddressTransactionBatchRequest getAddressTransactionBatchRequest, HttpServletResponse response) {
        transactionService.getAddressTransactionBatch(getAddressTransactionBatchRequest, response, true);
    }

    @GetMapping(value = "/lastTransactions")
    public ResponseEntity<IResponse> getLastTransactions() {
        return transactionService.getLastTransactions();
    }

    @GetMapping(value = "/total")
    public ResponseEntity<IResponse> getTotalTransactions() {
        return transactionService.getTotalTransactions();
    }

    @GetMapping()
    public ResponseEntity<IResponse> getTransactionsByPage(@RequestParam @Positive(message = EXPLORER_TRANSACTION_PAGE_INVALID) int page) {
        return transactionService.getTransactionsByPage(page);
    }

    @GetMapping(value = "/index")
    public ResponseEntity<TransactionIndexData> getCurrentIndex() {
        return ResponseEntity.ok(transactionIndexService.getLastTransactionIndexData());
    }
}
