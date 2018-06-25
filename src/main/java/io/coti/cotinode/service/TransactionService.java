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

import static io.coti.cotinode.http.HttpStringConstants.*;

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
                    .body(new AddTransactionResponse(
                            STATUS_ERROR,
                            AUTHENTICATION_FAILED_MESSAGE));
        }

        if (!balanceService.checkBalancesAndAddToPreBalance(request.baseTransactions)) {
            log.info("Pre balance check failed!");
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new AddTransactionResponse(
                            STATUS_ERROR,
                            INSUFFICIENT_FUNDS_MESSAGE));
        }

        TransactionData transactionData = new TransactionData(request);
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
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new AddTransactionResponse(
                        STATUS_SUCCESS,
                        TRANSACTION_CREATED_MESSAGE));
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