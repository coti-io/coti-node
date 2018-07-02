package io.coti.cotinode.controllers;

import io.coti.cotinode.exception.TransactionException;
import io.coti.cotinode.http.AddTransactionRequest;
import io.coti.cotinode.http.AddTransactionResponse;
import io.coti.cotinode.http.GetTransactionRequest;
import io.coti.cotinode.http.GetTransactionResponse;
import io.coti.cotinode.service.interfaces.IBalanceService;
import io.coti.cotinode.service.interfaces.ITransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static io.coti.cotinode.http.HttpStringConstants.STATUS_ERROR;
import static io.coti.cotinode.http.HttpStringConstants.TRANSACTION_ROLLBACK_MESSAGE;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

@Slf4j
@RestController
@RequestMapping("/transaction")
public class TransactionController {

    @Autowired
    private ITransactionService transactionService;

    @Autowired
    private IBalanceService balanceService;

    @RequestMapping(method = PUT)
    public ResponseEntity<AddTransactionResponse> addTransaction(@Valid @RequestBody AddTransactionRequest addTransactionRequest) {
        try {
            return transactionService.addNewTransaction(addTransactionRequest);
        }
        catch (TransactionException ex) {
            log.error("An error while adding transaction, performing a rollback procedure",ex);
            balanceService.rollbackBaseTransactions(addTransactionRequest.baseTransactions);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AddTransactionResponse(
                            STATUS_ERROR,
                            TRANSACTION_ROLLBACK_MESSAGE));
        }


    }

    @RequestMapping(method = POST)
    public ResponseEntity<GetTransactionResponse> getTransactionDetails(@Valid @RequestBody GetTransactionRequest getTransactionRequest) {
        return transactionService.getTransactionDetails(getTransactionRequest.transactionHash);
    }
}