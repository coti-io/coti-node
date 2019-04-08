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
//        if( parentProcessingTransactions == null)
//            return false;
        return parentProcessingTransactions.get(transactionHash) != null;

    }

    @Override
    public void handlePropagatedTransaction(TransactionData transactionData) {
         log.info("Process start: {}", transactionData.getHash());
        if (transactionHelper.isTransactionAlreadyPropagated(transactionData)) {
            log.debug("Transaction already exists: {}", transactionData.getHash());
            return;
        }
        List<Hash> childrenTransactionHashes = transactionData.getChildrenTransactionHashes();
//        log.info("[Start print] Transaction: {} has a child transaction: {}", transactionData.getHash(), childrenTransactionHashes.get(0));
        try {
            synchronized (transactionData) {
                log.info("Starting synchronized section for transaction {}", transactionData.getHash() );
                transactionHelper.startHandleTransaction(transactionData);
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
                    if (!hasOneOfParentsMissing(transactionData)) {
                        postponedTransactions.remove(transactionData.getHash());
                    } else {
                        log.info("Transaction {} was postponed due to missing parent", transactionData.getHash() );
                        return;
                    }
                }
                if (!validationService.validateBalancesAndAddToPreBalance(transactionData)) {
                    log.error("Balance check failed: {}", transactionData.getHash());
                    return;
                }
                transactionHelper.attachTransactionToCluster(transactionData);
                transactionHelper.setTransactionStateToSaved(transactionData);

                continueHandlePropagatedTransaction(transactionData);
                transactionHelper.setTransactionStateToFinished(transactionData);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            boolean isTransactionFinished = transactionHelper.isTransactionFinished(transactionData);
            transactionHelper.endHandleTransaction(transactionData);
            log.info("In finally section for transaction: {}, which is finished [{}], childrenTransactionHashes size is: {}",transactionData.getHash(), isTransactionFinished, childrenTransactionHashes.size());
            childrenTransactionHashes.forEach(childTransactionHash -> {
                log.info("[End print] Transaction: {} was done [{}], has a child transaction: {}", transactionData.getHash(), isTransactionFinished, childTransactionHash);
                TransactionData parentProcessingChildTransaction = parentProcessingTransactions.get(childTransactionHash);
                if (parentProcessingChildTransaction != null) {
                    synchronized (parentProcessingChildTransaction) {
                        parentProcessingChildTransaction.notify();
                        parentProcessingTransactions.remove(childTransactionHash);
                    }
                }
                if (isTransactionFinished)
                    releasePostponedChildTransaction(transactionData, childTransactionHash);
            });
            log.info("Exiting finally section for transaction: {}", transactionData.getHash());
        }

    }

    private void releasePostponedChildTransaction(TransactionData transactionData, Hash childTransactionHash) {
        TransactionData postponedChildrenTransaction = postponedTransactions.get(childTransactionHash);
        if (postponedChildrenTransaction != null) {
            synchronized (postponedChildrenTransaction) {
                if (transactions.getByHash(transactionData.getHash()) != null) {
                    log.debug("Handling postponed transaction : {}, child of transaction: {}", postponedChildrenTransaction.getHash(), transactionData.getHash());
                    postponedTransactions.remove(childTransactionHash);
                    new Thread(() -> handlePropagatedTransaction(postponedChildrenTransaction)).start();
                    if( postponedTransactions.get(childTransactionHash) != null )
                        log.info("Transaction {} was meant to be removed, but still exists in postponed list of size {}", childTransactionHash, postponedTransactions.size() );
                } else {
                    log.info("Transaction: {} Finally not yet in DB, when trying to release postponed child: {}, size of transactions awaiting release: {}", transactionData.getHash(), childTransactionHash, postponedTransactions.size());
                }
            }
        }
    }


    protected void continueHandlePropagatedTransaction(TransactionData transactionData) {
    }

    public void addToExplorerIndexes(TransactionData transactionData) {

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
