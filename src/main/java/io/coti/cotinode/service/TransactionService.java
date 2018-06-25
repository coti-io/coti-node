package io.coti.cotinode.service;

import io.coti.cotinode.crypto.CryptoUtils;
import io.coti.cotinode.data.BaseTransactionData;
import io.coti.cotinode.data.ConfirmationData;
import io.coti.cotinode.data.Hash;
import io.coti.cotinode.data.TransactionData;
import io.coti.cotinode.http.AddTransactionRequest;
import io.coti.cotinode.http.AddTransactionResponse;
import io.coti.cotinode.model.Transactions;
import io.coti.cotinode.service.interfaces.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Queue;

import static io.coti.cotinode.http.HttpStringConstants.STATUS_ERROR;
import static io.coti.cotinode.http.HttpStringConstants.STATUS_SUCCESS;

@Slf4j
@Service
public class TransactionService implements ITransactionService {

    @Autowired
    private IBalanceService balanceService;
    @Autowired
    private ICluster clusterService;
    @Autowired
    private IValidationService validationService;
    @Autowired
    private Transactions transactions;


    @Override
    public ResponseEntity<AddTransactionResponse> addNewTransaction(AddTransactionRequest request) {
        if (!validateAddresses(request)) {
            log.info("Failed to validate addresses!");
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new AddTransactionResponse(request.transactionHash, STATUS_ERROR));
        }

        if (!balanceService.checkBalancesAndAddToPreBalance(request.baseTransactions)) {
            log.info("Pre balance check failed!");
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new AddTransactionResponse(request.transactionHash, STATUS_ERROR));
        }

        handleTransactionAsync(new TransactionData(request.transactionHash));

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new AddTransactionResponse(request.transactionHash, STATUS_SUCCESS));
    }

    private void handleTransactions (TransactionData transactionData) {
        transactionData = clusterService.selectSources(transactionData);
        if (!validationService.validateSource(transactionData.getLeftParentHash()) ||
                !validationService.validateSource(transactionData.getRightParentHash())) {
            log.info("Unvalidated transaction source");
        }

        //POW:
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        transactions.put(transactionData);

        balanceService.insertIntoUnconfirmedDBandAddToTccQeueue(new ConfirmationData(transactionData.getHash()));
    }

    void handleTransactionAsync(TransactionData transactionDataToProcess) {
        class HandleTransactionsRunnable implements Runnable {
            TransactionData transactionDataToProcess;
            HandleTransactionsRunnable(TransactionData transactionData) { transactionDataToProcess = transactionData; }
            public void run() {
                handleTransactions(transactionDataToProcess);
            }
        }
        Thread t = new Thread(new HandleTransactionsRunnable(transactionDataToProcess));
        t.start();
    }


    private boolean validateAddresses(AddTransactionRequest request) {
        for (BaseTransactionData baseTransactionData :
                request.baseTransactions) {
            if (!validationService.validateSenderAddress(
                    request.message,
                    CryptoUtils.convertSignatureFromString(baseTransactionData.getSignature()),
                    baseTransactionData.getAddressHash())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public TransactionData getTransactionData(Hash transactionHash) {
        return transactions.getByHash(transactionHash);
    }
}