package io.coti.historynode.services;

import io.coti.basenode.data.TransactionData;
import io.coti.basenode.services.BaseNodeTransactionService;
import io.coti.basenode.services.interfaces.IValidationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
public class TransactionService extends BaseNodeTransactionService {

    private Queue<TransactionData> transactionsToValidate;
    private AtomicBoolean isValidatorRunning;

    @Autowired
    private IValidationService validationService;
    @Autowired
    private TransactionIndexingService transactionIndexingService;

    @Override
    public void init() {
        transactionsToValidate = new PriorityQueue<>();
        isValidatorRunning = new AtomicBoolean(false);
        super.init();
    }


    @Scheduled(fixedRate = 1000)
    private void checkAttachedTransactions() {
        if (!isValidatorRunning.compareAndSet(false, true)) {
            return;
        }
        while (!transactionsToValidate.isEmpty()) {
            TransactionData transactionData = transactionsToValidate.remove();
            log.debug("History node Fully Checking transaction: {}", transactionData.getHash());
            if( validationService.fullValidation(transactionData) ) {
                // Update Indexing structures with details from the new transaction
                transactionIndexingService.addToHistoryTransactionIndexes(transactionData);
            }
        }
        isValidatorRunning.set(false);
    }

    @Override
    protected void continueHandlePropagatedTransaction(TransactionData transactionData) {
        super.continueHandlePropagatedTransaction(transactionData);
        log.debug("Continue to handle propagated transaction {} by history node", transactionData.getHash());
        transactionsToValidate.add(transactionData);
    }




}
