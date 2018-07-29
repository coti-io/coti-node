package io.coti.dspnode.services;

import io.coti.common.communication.interfaces.ISender;
import io.coti.common.communication.interfaces.IPropagationPublisher;
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

@Slf4j
@Service
public class TransactionService {
    Queue<TransactionData> transactionsToValidate;
    AtomicBoolean isValidatorRunning;
    List<TransactionData> transactionsInProcess;

    @Autowired
    private TransactionHelper transactionHelper;
    @Autowired
    private IPropagationPublisher propagationPublisher;
    @Autowired
    private IBalanceService balanceService;
    @Autowired
    private IValidationService validationService;
    @Autowired
    private Transactions transactions;
    @Autowired
    private ISender transactionSender;

    public String handleNewTransactionFromFullNode(TransactionData transactionData) {
        log.info("Running new transactions from full node handler");
        if (transactionHelper.isTransactionExists(transactionData.getHash()) ||
                transactionsInProcess.contains(transactionData)) {
            log.info("Transaction already exists");
            return "Transaction Exists: " + transactionData.getHash();
        }
        if (!transactionHelper.validateDataIntegrity(transactionData) ||
                !balanceService.checkBalancesAndAddToPreBalance(transactionData.getBaseTransactions()) ||
                !validationService.validatePow(transactionData)) {
            return "Invalid Transaction Received: " + transactionData.getHash();
        }
        transactionsInProcess.add(transactionData);
        propagationPublisher.propagateTransaction(transactionData, TransactionData.class.getName() + "Full Nodes");
        propagationPublisher.propagateTransaction(transactionData, TransactionData.class.getName() + "DSP Nodes");
        transactions.put(transactionData);
        transactionsToValidate.add(transactionData);
        checkAttachedTransactions();
        return "Received Transaction: " + transactionData.getHash();
    }

    private void checkAttachedTransactions() {
        if (isValidatorRunning.compareAndSet(false, true)) {
            return;
        }
        while (!transactionsToValidate.isEmpty()) {
            TransactionData transactionData = transactionsToValidate.remove();
            boolean result = validationService.fullValidation(transactionData);
            transactionData.addSignature("Node ID", result);
            transactionsInProcess.remove(transactionData);
        }
        isValidatorRunning.set(false);
    }

    @PostConstruct
    private void init() {
        transactionsToValidate = new PriorityQueue<>();
        isValidatorRunning = new AtomicBoolean(false);
        transactionsInProcess = new LinkedList<>();
    }

    public void handlePropagatedTransaction(TransactionData transactionData) {
        log.info("Received new propagated Address: {}", transactionData);
        if (transactionHelper.isTransactionExists(transactionData.getHash())) {
            log.info("Transaction already exists");
            return;
        }
        if (!transactionHelper.validateDataIntegrity(transactionData)) {
            log.info("Data Integrity validation failed");
            return;
        }
        transactions.put(transactionData);
        propagationPublisher.propagateTransaction(transactionData, TransactionData.class.getName() + "Full Nodes");
        if (!transactionHelper.checkBalancesAndAddToPreBalance(transactionData.getBaseTransactions())) {
            transactionData.addSignature("Node ID", false); // TODO: replace with a sign mechanism
            transactionSender.sendTransaction(transactionData);
        }
    }
}
