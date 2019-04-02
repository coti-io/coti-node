package io.coti.basenode.services;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.interfaces.ITransactionHelper;
import io.coti.basenode.services.interfaces.ITransactionService;
import io.coti.basenode.services.interfaces.IValidationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class BaseNodeTransactionService implements ITransactionService {

    @Autowired
    private ITransactionHelper transactionHelper;
    @Autowired
    private IValidationService validationService;
    @Autowired
    private Transactions transactions;
    protected Map<Hash, TransactionData> parentProcessingTransactions = new ConcurrentHashMap<>();
    protected Map<Hash, TransactionData> postponedTransactions = new ConcurrentHashMap<>();

    @Override
    public void init() {
        log.info("{} is up", this.getClass().getSimpleName());
    }

    public boolean isTransactionInParentMap(Hash transactionHash) {
        return parentProcessingTransactions.get(transactionHash) != null;
    }

    @Override
    public void handlePropagatedTransaction(TransactionData transactionData) {
       // log.info("Process start: {}", transactionData.getHash());
        if (transactionHelper.isTransactionAlreadyPropagated(transactionData)) {
            log.debug("Transaction already exists: {}", transactionData.getHash());
            return;
        }
        List<Hash> childrenTransactionHashes = transactionData.getChildrenTransactionHashes();
        try {
            transactionHelper.startHandleTransaction(transactionData);
            childrenTransactionHashes.forEach(childrenTransactionHash -> releasePostponedChildTransaction(transactionData, childrenTransactionHash));
            while (hasOneOfParentsProcessing(transactionData)) {
                parentProcessingTransactions.put(transactionData.getHash(), transactionData);
                synchronized (transactionData) {
                 //   log.info("start wait: {}", transactionData.getHash());
                    transactionData.wait();
                //    log.info("end wait: {}", transactionData.getHash());
                }
            }
            if (!validationService.validatePropagatedTransactionDataIntegrity(transactionData)) {
                log.error("Data Integrity validation failed: {}", transactionData.getHash());
                return;
            }
            if (hasOneOfParentsMissing(transactionData)) {
                postponedTransactions.put(transactionData.getHash(), transactionData);
                return;
            }
            if (!validationService.validateBalancesAndAddToPreBalance(transactionData)) {
                log.error("Balance check failed: {}", transactionData.getHash());
                return;
            }
            transactionHelper.attachTransactionToCluster(transactionData);
            transactionHelper.setTransactionStateToSaved(transactionData);

            continueHandlePropagatedTransaction(transactionData);
            transactionHelper.setTransactionStateToFinished(transactionData);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            transactionHelper.endHandleTransaction(transactionData);
            childrenTransactionHashes.forEach(childTransactionHash -> {
                TransactionData parentProcessingChildTransaction = parentProcessingTransactions.get(childTransactionHash);
                if (parentProcessingChildTransaction != null) {
                    synchronized (parentProcessingChildTransaction) {
                        parentProcessingChildTransaction.notify();
                        parentProcessingTransactions.remove(childTransactionHash);
                    }
                }
                releasePostponedChildTransaction(transactionData, childTransactionHash);
            });


        }

    }

    private void releasePostponedChildTransaction(TransactionData transactionData, Hash childTransactionHash) {
        TransactionData postponedChildrenTransaction = postponedTransactions.get(childTransactionHash);
        if (postponedChildrenTransaction != null) {
            log.debug("Handling postponed transaction : {}, child of transaction: {}", postponedChildrenTransaction.getHash(), transactionData.getHash());
            postponedTransactions.remove(childTransactionHash);
            new Thread(() -> handlePropagatedTransaction(postponedChildrenTransaction)).start();
        }
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

    @Override
    public long incrementAndGetExplorerIndex() {
        return 0; // Relevant only for Full Node
    }
}
