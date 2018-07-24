package io.coti.dspnode.services;

import io.coti.common.communication.interfaces.ITransactionSender;
import io.coti.common.communication.interfaces.publisher.ITransactionPropagationPublisher;
import io.coti.common.communication.interfaces.ITransactionPropagationSubscriber;
import io.coti.common.communication.interfaces.ITransactionReceiver;
import io.coti.common.data.TransactionData;
import io.coti.common.model.Transactions;
import io.coti.common.services.TransactionHelper;
import io.coti.common.services.interfaces.IBalanceService;
import io.coti.common.services.interfaces.IValidationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
@Service
public class TransactionService {
    Queue<TransactionData> transactionsToValidate;
    AtomicBoolean isValidatorRunning;
    List<TransactionData> transactionsInProcess;

    @Autowired
    private ITransactionReceiver transactionReceiver;
    @Autowired
    private TransactionHelper transactionHelper;
    @Autowired
    private ITransactionPropagationPublisher transactionPropagationPublisher;
    @Autowired
    private ITransactionPropagationSubscriber transactionPropagationSubscriber;
    @Autowired
    private IBalanceService balanceService;
    @Autowired
    private IValidationService validationService;
    @Autowired
    private Transactions transactions;
    @Autowired
    private ITransactionSender transactionSender;

    private Function<TransactionData, String> newTransactionFromFullNodeHandler = transactionData -> {
        log.info("Running new transactions from full node handler");
        if (transactionHelper.isTransactionExists(transactionData.getHash()) ||
                transactionsInProcess.contains(transactionData)){
            log.info("Transaction already exists");
            return "Transaction Exists: " + transactionData.getHash();
        }
        if(!transactionHelper.validateDataIntegrity(transactionData) ||
                !balanceService.checkBalancesAndAddToPreBalance(transactionData.getBaseTransactions()) ||
                !validationService.validatePow(transactionData)) {
            return "Invalid Transaction Received: " + transactionData.getHash();
        }
        transactionsInProcess.add(transactionData);
        transactionPropagationPublisher.propagateTransactionToDSPs(transactionData);
        transactionPropagationPublisher.propagateTransactionToFullNodes(transactionData);
        transactions.put(transactionData);
        transactionsToValidate.add(transactionData);
        checkAttachedTransactions();
        return "Received Transaction: " + transactionData.getHash();
    };
    private Consumer<TransactionData> propagatedTransactionsFromDSPsHandler = new Consumer<TransactionData>() {
        @Override
        public void accept(TransactionData transactionData) {
            if (transactionHelper.isTransactionExists(transactionData.getHash())) {
                log.info("Transaction already exists");
                return;
            }
            if (!transactionHelper.validateDataIntegrity(transactionData)) {
                log.info("Data Integrity validation failed");
                return;
            }
            transactions.put(transactionData);
            transactionPropagationPublisher.propagateTransactionToFullNodes(transactionData);
            if(!transactionHelper.checkBalancesAndAddToPreBalance(transactionData.getBaseTransactions())){
                transactionData.addSignature("Node ID", false); // TODO: replace with a sign mechanism
                transactionSender.sendTransaction(transactionData);
            }
        }
    };

    private void checkAttachedTransactions() {
        if (isValidatorRunning.compareAndSet(false, true)) {
            return;
        }
        while (!transactionsToValidate.isEmpty()) {
            TransactionData transactionData = transactionsToValidate.remove();
            boolean result = validationService.fullValidation(transactionData);
            transactionData.addSignature("Node ID", result);
//            transactionSender.sendTransaction(transactionData);
            transactionsInProcess.remove(transactionData);
        }
        isValidatorRunning.set(false);
    }

    @PostConstruct
    private void init() {
        transactionReceiver.init(newTransactionFromFullNodeHandler);
        transactionsToValidate = new PriorityQueue<>();
        isValidatorRunning = new AtomicBoolean(false);
        transactionPropagationSubscriber.init(propagatedTransactionsFromDSPsHandler, "DSPs");
        transactionsInProcess = new LinkedList<>();
    }
}