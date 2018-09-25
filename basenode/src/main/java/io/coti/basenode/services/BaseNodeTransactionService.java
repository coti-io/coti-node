package io.coti.basenode.services;

import io.coti.basenode.crypto.TransactionCrypto;
import io.coti.basenode.data.Hash;
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

import static java.lang.Thread.sleep;

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
    public void handlePropagatedTransaction(TransactionData transactionData) throws InterruptedException {
        try {
            transactionHelper.startHandleTransaction(transactionData);
            if (!transactionHelper.isTransactionExists(transactionData)) {
                log.info("Transaction already exists: {}", transactionData.getHash());
                return;
            }
            if (!transactionHelper.validateTransaction(transactionData) ||
                    !transactionCrypto.verifySignature(transactionData) ||
                    !validationService.validatePot(transactionData)) {
                log.error("Data Integrity validation failed: {}", transactionData.getHash());
                return;
            }
            if (hasOneOfParentsProcessing(transactionData)) {
                Hash transactionHash = transactionData.getHash();
                synchronized (transactionHash) {
                    while (!hasOneOfParentsProcessing(transactionData)) {
                        transactionHash.wait();
                    }
                }
            }
            if (hasOneOfParentsMissing(transactionData)) {
                postponedTransactions.add(transactionData);
                return;
            }
            if (!transactionHelper.checkBalancesAndAddToPreBalance(transactionData)) {
                log.error("Balance check failed: {}", transactionData.getHash());
                return;
            }

            transactionHelper.attachTransactionToCluster(transactionData);
            transactionHelper.setTransactionStateToSaved(transactionData);

            continueHandlePropagatedTransaction(transactionData);
            transactionHelper.setTransactionStateToFinished(transactionData);
            List<TransactionData> postponedParentTransactions = postponedTransactions.stream().filter(
                    postponedTransactionData ->
                            (postponedTransactionData.getRightParentHash() != null && postponedTransactionData.getRightParentHash().equals(transactionData.getHash())) ||
                                    (postponedTransactionData.getLeftParentHash() != null && postponedTransactionData.getLeftParentHash().equals(transactionData.getHash())))
                    .collect(Collectors.toList());
            postponedParentTransactions.forEach(postponedTransaction -> {
                postponedTransactions.remove(postponedTransaction);
                try {
                    log.debug("Handling postponed transaction : {}, parent of transaction: {}", postponedTransaction.getHash(), transactionData.getHash());
                    handlePropagatedTransaction(postponedTransaction);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }  finally {
            transactionHelper.endHandleTransaction(transactionData);
            if (transactionData.getChildrenTransactions() != null) {
                for (Hash childrenTransactionHash : transactionData.getChildrenTransactions()) {
                    synchronized (childrenTransactionHash) {
                     //   log.info("{} is notified",childrenTransactionHash);
                        childrenTransactionHash.notify();
                    }
                }
            }
        }

    }

    protected void continueHandlePropagatedTransaction(TransactionData transactionData) {
    }

    private boolean hasOneOfParentsProcessing(TransactionData transactionData) {
        return (transactionData.getLeftParentHash() != null && transactionHelper.isTransactionProcessing(transactionData.getLeftParentHash())) ||
                (transactionData.getRightParentHash() != null && transactionHelper.isTransactionProcessing(transactionData.getRightParentHash()));
    }

    private boolean hasOneOfParentsMissing(TransactionData transactionData) {
        return (transactionData.getLeftParentHash() != null && transactions.getByHash(transactionData.getLeftParentHash()) == null) ||
                (transactionData.getRightParentHash() != null && transactions.getByHash(transactionData.getRightParentHash()) == null);
    }

    public int totalPostponedTransactions() {
        return postponedTransactions.size();
    }
}
