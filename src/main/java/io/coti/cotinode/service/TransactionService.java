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

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

import static io.coti.cotinode.http.HttpStringConstants.*;

@Slf4j
@Service
public class TransactionService implements ITransactionService {

    @Autowired
    private IZeroSpendService zeroSpendService;
    @Autowired
    private IBalanceService balanceService;
    @Autowired
    private IClusterService clusterService;
    @Autowired
    private IValidationService validationService;
    @Autowired
    private Transactions transactions;

    @PostConstruct
    private void init(){
        log.info("Transaction service Started");
    }

    @Override
    public ResponseEntity<AddTransactionResponse> addNewTransaction(AddTransactionRequest request) {
        log.info("New transaction request is being processed. Transaction Hash: {}", request.transactionHash);
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

        transactionData = selectSources(transactionData);

        if (!validationService.validateSource(transactionData.getLeftParentHash()) ||
                !validationService.validateSource(transactionData.getRightParentHash())) {
            log.info("Could not validate transaction source");
        }

        //POW:
        try {
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        createNewSourceTransaction(transactionData);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new AddTransactionResponse(
                        STATUS_SUCCESS,
                        TRANSACTION_CREATED_MESSAGE));
    }

    private TransactionData selectSources(TransactionData transactionData) {
        transactionData = clusterService.selectSources(transactionData);
        if (transactionData.hasSources()) {
            return transactionData;
        }

        log.info("No sources found for transaction with trust score {}", transactionData.getSenderTrustScore());
        int retryTimes = 200 / transactionData.getRoundedSenderTrustScore();
        while (!transactionData.hasSources() && retryTimes > 0) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            retryTimes--;
            transactionData = clusterService.selectSources(transactionData);
            if (transactionData.hasSources()) {
                return transactionData;
            }
        }

        TransactionData zeroSpendTransaction = zeroSpendService.getZeroSpendTransaction(transactionData.getSenderTrustScore());
        createNewSourceTransaction(zeroSpendTransaction);
        clusterService.addTransactionDataToSources(zeroSpendTransaction);
        transactionData = clusterService.selectSources(transactionData);
        while (!transactionData.hasSources()) {
            log.info("Waiting 2 seconds for new zero spend transaction to be added to available sources");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            transactionData = clusterService.selectSources(transactionData);
        }
        return transactionData;
    }

    private void createNewSourceTransaction(TransactionData transactionData) {
        transactions.put(transactionData);
        balanceService.insertIntoUnconfirmedDBandAddToTccQeueue(new ConfirmationData(transactionData.getHash()));
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