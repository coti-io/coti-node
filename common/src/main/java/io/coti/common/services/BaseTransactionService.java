package io.coti.common.services;

import io.coti.common.crypto.TransactionCrypto;
import io.coti.common.data.TransactionData;
import io.coti.common.model.Transactions;
import io.coti.common.services.interfaces.ITransactionHelper;
import io.coti.common.services.interfaces.IValidationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;

@Slf4j
@Service
public abstract class BaseTransactionService {

    @Autowired
    private ITransactionHelper transactionHelper;
    @Autowired
    private TransactionCrypto transactionCrypto;
    @Autowired
    private IValidationService validationService;
    @Autowired
    private Transactions transactions;
    private List<TransactionData> postponedTransactions = new LinkedList<>();

    public void handlePropagatedTransaction(TransactionData transactionData){
        if (!transactionHelper.startHandleTransaction(transactionData)) {
            log.debug("Transaction already exists: {}", transactionData.getHash().toHexString());
            return;
        }
        if (!transactionHelper.validateTransaction(transactionData) ||
                !transactionCrypto.verifySignature(transactionData) ||
                !validationService.validatePow(transactionData)) {
            log.error("Data Integrity validation failed: {}", transactionData.getHash().toHexString());
            return;
        }
        if (hasOneOfParentsNotArrivedYet(transactionData)) {
            postponedTransactions.add(transactionData);
            return;
        }
        if (!transactionHelper.checkBalancesAndAddToPreBalance(transactionData)) {
            log.error("Balance check failed: {}", transactionData.getHash().toHexString());
            return;
        }
        transactionHelper.attachTransactionToCluster(transactionData);
        transactionHelper.setTransactionStateToSaved(transactionData);

        continueHandlePropagatedTransaction(transactionData);

        TransactionData postponedTransaction = postponedTransactions.stream().filter(
                postponedTransactionData ->
                        postponedTransactionData.getRightParentHash().equals(transactionData.getHash()) ||
                                postponedTransactionData.getLeftParentHash().equals(transactionData.getHash()))
                .findFirst().orElse(null);
        if(postponedTransaction != null){
            postponedTransactions.remove(postponedTransaction);
            handlePropagatedTransaction(transactionData);
        }
        transactionHelper.setTransactionStateToFinished(transactionData);
        transactionHelper.endHandleTransaction(transactionData);
    }

    protected abstract void continueHandlePropagatedTransaction(TransactionData transactionData);

    private boolean hasOneOfParentsNotArrivedYet(TransactionData transactionData) {
        return (transactionData.getLeftParentHash() != null && transactions.getByHash(transactionData.getLeftParentHash()) == null) ||
                transactionData.getRightParentHash() != null && transactions.getByHash(transactionData.getRightParentHash()) == null;
    }
}
