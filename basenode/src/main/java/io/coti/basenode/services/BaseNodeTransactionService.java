package io.coti.basenode.services;

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
        try {
            if (transactionHelper.isTransactionAlreadyPropagated(transactionData)) {
                log.debug("Transaction already exists: {}", transactionData.getHash());
                return;
            }
            transactionHelper.startHandleTransaction(transactionData);
            if (!validationService.validatePropagatedTransactionDataIntegrity(transactionData)) {
                log.error("Data Integrity validation failed: {}", transactionData.getHash());
                return;
            }
            if (hasOneOfParentsMissing(transactionData)) {
                postponedTransactions.add(transactionData);
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

        }
        catch (Exception e){
            log.error("Exception",e);
        }

        finally {
            transactionHelper.endHandleTransaction(transactionData);
        }

    }

    protected void continueHandlePropagatedTransaction(TransactionData transactionData) {
    }

    private boolean hasOneOfParentsMissing(TransactionData transactionData) {
        return (transactionData.getLeftParentHash() != null && transactions.getByHash(transactionData.getLeftParentHash()) == null) ||
                (transactionData.getRightParentHash() != null && transactions.getByHash(transactionData.getRightParentHash()) == null);
    }

    public int totalPostponedTransactions() {
        return postponedTransactions.size();
    }
}
