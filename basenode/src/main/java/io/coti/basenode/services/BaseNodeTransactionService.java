package io.coti.basenode.services;

import io.coti.basenode.crypto.TransactionCrypto;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.interfaces.ITransactionHelper;
import io.coti.basenode.services.interfaces.ITransactionService;
import io.coti.basenode.services.interfaces.IValidationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BaseNodeTransactionService implements ITransactionService {

    @Autowired
    private ITransactionHelper transactionHelper;
    @Autowired
    private TransactionCrypto transactionCrypto;
    @Autowired
    private IValidationService validationService;
    @Autowired
    private Transactions transactions;
    private List<TransactionData> postponedTransactions = new LinkedList<>();

    @Override
    public void init() {
        log.info("{} is up", this.getClass().getSimpleName());
    }

    @Override
    public void handlePropagatedTransaction(TransactionData transactionData) {
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
        if (hasOneOfParentsMissing(transactionData)) {
            postponedTransactions.add(transactionData);
            log.info("{}", postponedTransactions.size());
            return;
        }
        if (!transactionHelper.checkBalancesAndAddToPreBalance(transactionData)) {
            log.error("Balance check failed: {}", transactionData.getHash().toHexString());
            return;
        }
        transactionHelper.attachTransactionToCluster(transactionData);
        transactionHelper.setTransactionStateToSaved(transactionData);

        continueHandlePropagatedTransaction(transactionData);

        List<TransactionData> postponedParentTransactions = postponedTransactions.stream().filter(
                postponedTransactionData ->
                        postponedTransactionData.getRightParentHash().equals(transactionData.getHash()) ||
                                postponedTransactionData.getLeftParentHash().equals(transactionData.getHash()))
                .collect(Collectors.toList());
        postponedParentTransactions.forEach(postponedTransaction -> {
            postponedTransactions.remove(postponedTransaction);
            handlePropagatedTransaction(postponedTransaction);
        });
        transactionHelper.setTransactionStateToFinished(transactionData);
        transactionHelper.endHandleTransaction(transactionData);
    }

    protected void continueHandlePropagatedTransaction(TransactionData transactionData) {
    }

    private boolean hasOneOfParentsMissing(TransactionData transactionData) {
        return (transactionData.getLeftParentHash() != null && transactions.getByHash(transactionData.getLeftParentHash()) == null) ||
                transactionData.getRightParentHash() != null && transactions.getByHash(transactionData.getRightParentHash()) == null;
    }
}
