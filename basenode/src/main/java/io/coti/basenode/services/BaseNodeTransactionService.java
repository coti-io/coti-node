package io.coti.basenode.services;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.data.TransactionType;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.interfaces.ITransactionHelper;
import io.coti.basenode.services.interfaces.ITransactionService;
import io.coti.basenode.services.interfaces.IValidationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BaseNodeTransactionService implements ITransactionService {

    @Autowired
    private ITransactionHelper transactionHelper;
    @Autowired
    private IValidationService validationService;
    @Autowired
    private Transactions transactions;
    private Map<Hash, TransactionData> parentProcessingTransactions = new ConcurrentHashMap<>();
    private List<TransactionData> postponedTransactions = new LinkedList<>();

    @Override
    public void init() {
        log.info("{} is up", this.getClass().getSimpleName());
    }

    @Override
    public void handlePropagatedTransaction(TransactionData transactionData) {
        if (transactionHelper.isTransactionAlreadyPropagated(transactionData)) {
            log.debug("Transaction already exists: {}", transactionData.getHash());
            return;
        }
        List<Hash> childrenTransactions = transactionData.getChildrenTransactions();
        try {
            transactionHelper.startHandleTransaction(transactionData);
            while (hasOneOfParentsProcessing(transactionData)) {
                parentProcessingTransactions.put(transactionData.getHash(), transactionData);
                synchronized (transactionData) {
                    transactionData.wait();
                }
            }
            if (transactionData.getType().equals(TransactionType.ZeroSpend)) {
                if(!validateZeroSpendTransaction(transactionData)) {
                    return;
                }

            } else {
                if(!validateNonZeroSpendTransaction(transactionData)) {
                    return;
                }
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
                log.debug("Handling postponed transaction : {}, parent of transaction: {}", postponedTransaction.getHash(), transactionData.getHash());
                postponedTransactions.remove(postponedTransaction);
                handlePropagatedTransaction(postponedTransaction);
            });
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            transactionHelper.endHandleTransaction(transactionData);
            for (Hash childrenTransactionHash : childrenTransactions) {
                TransactionData childrenTransaction = parentProcessingTransactions.get(childrenTransactionHash);
                if (childrenTransaction != null)
                    synchronized (childrenTransaction) {
                        childrenTransaction.notify();
                        parentProcessingTransactions.remove(childrenTransactionHash);
                    }
            }
        }

    }

    private boolean validateNonZeroSpendTransaction(TransactionData transactionData) {
        if (!validationService.validatePropagatedTransactionDataIntegrity(transactionData)) {
            log.error("Data Integrity validation failed: {}", transactionData.getHash());
            return false;
        }
        if (hasOneOfParentsMissing(transactionData)) {
            postponedTransactions.add(transactionData);
            return false;
        }
        if (!returnAfterBalanceValidation(transactionData)) {
            return false;
        }
        return true;
    }

    private boolean validateZeroSpendTransaction(TransactionData transactionData) {
        if (!validationService.validateTransactionDataIntegrity(transactionData)
                || !validationService.validateTransactionNodeSignature(transactionData)) {
            log.error("Data Integrity validation failed: {}", transactionData.getHash());
            return false;
        }
        if (hasOneOfParentsMissing(transactionData)) {
            postponedTransactions.add(transactionData);
            return false;
        }
        return true;
    }

    protected boolean returnAfterBalanceValidation(TransactionData transactionData) {
        if (!validationService.validateBalancesAndAddToPreBalance(transactionData)) {
            log.error("Balance check failed: {}", transactionData.getHash());
            return false;
        }
        transactionData.setValid(true);
        return true;
    }

    protected void continueHandlePropagatedTransaction(TransactionData transactionData) {
    }

    private boolean hasOneOfParentsProcessing(TransactionData transactionData) {
        return (transactionData.getLeftParentHash() != null && transactionHelper.isTransactionHashProcessing(transactionData.getLeftParentHash())) ||
                (transactionData.getRightParentHash() != null && transactionHelper.isTransactionHashProcessing(transactionData.getRightParentHash()));
    }

    private boolean hasOneOfParentsMissing(TransactionData transactionData) {
        return (transactionData.getLeftParentHash() != null && transactions.getByHash(transactionData.getLeftParentHash()) == null) ||
                (transactionData.getRightParentHash() != null && transactions.getByHash(transactionData.getRightParentHash()) == null);
    }

    public int totalPostponedTransactions() {
        return postponedTransactions.size();
    }
}
