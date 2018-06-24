package io.coti.cotinode.service;

import io.coti.cotinode.crypto.CryptoUtils;
import io.coti.cotinode.data.BaseTransactionData;
import io.coti.cotinode.data.Hash;
import io.coti.cotinode.data.TransactionData;
import io.coti.cotinode.http.AddTransactionRequest;
import io.coti.cotinode.http.AddTransactionResponse;
import io.coti.cotinode.model.Transactions;
import io.coti.cotinode.service.interfaces.ITransactionService;
import io.coti.cotinode.service.interfaces.IValidationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import static io.coti.cotinode.http.HttpStringConstants.STATUS_ERROR;
import static io.coti.cotinode.http.HttpStringConstants.STATUS_SUCCESS;

@Slf4j
@Service
public class TransactionService implements ITransactionService {

    @Autowired
    private BalanceService balanceService;
    @Autowired
    private ClusterService clusterService;
    @Autowired
    private IValidationService validationService;
    @Autowired
    private Transactions transactions;

    @Override
    public ResponseEntity<AddTransactionResponse> addNewTransaction(AddTransactionRequest request) {
        TransactionData transactionData = new TransactionData(request.transactionHash);
        if (!validateAddresses(request)) {
            log.info("Failed to validate addresses!");
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new AddTransactionResponse(request.transactionHash, STATUS_ERROR));
        }

//        if (!balanceService.checkBalancesAndAddToPreBalance(request.transferredAmounts)) {
//            return false;
//        }

        //Transaction = Attach to sources

//        validationService.validateSource(transactionData.getLeftParentHash());
//        validationService.validateSource(transactionData.getRightParentHash());
        transactions.put(transactionData);

//        balanceService.dbSync();

//
//        balanceService.addToPreBalance(transactionData);
//        transactionData = clusterService.addToCluster(transactionData);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new AddTransactionResponse(request.transactionHash, STATUS_SUCCESS));
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