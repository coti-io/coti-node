package io.coti.dspnode.services;

import io.coti.common.communication.DspVote;
import io.coti.common.communication.interfaces.IPropagationPublisher;
import io.coti.common.communication.interfaces.ISender;
import io.coti.common.crypto.CryptoHelper;
import io.coti.common.crypto.NodeCryptoHelper;
import io.coti.common.data.TransactionData;
import io.coti.common.model.Transactions;
import io.coti.common.services.TransactionHelper;
import io.coti.common.services.interfaces.IBalanceService;
import io.coti.common.services.interfaces.IValidationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
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
    private ISender sender;

    public String handleNewTransactionFromFullNode(TransactionData transactionData) {
        log.info("Running new transactions from full node handler");
        if (!transactionHelper.startHandleTransaction(transactionData)) {
            log.info("Transaction already exists");
            return "Transaction Exists: " + transactionData.getHash();
        }
        if (!transactionHelper.validateDataIntegrity(transactionData) ||
                !NodeCryptoHelper.verifyTransactionSignature(transactionData) ||
                !validationService.validatePow(transactionData) ||
                !balanceService.checkBalancesAndAddToPreBalance(transactionData.getBaseTransactions())) {
            log.info("Invalid Transaction Received!");
            return "Invalid Transaction Received: " + transactionData.getHash();
        }
        propagationPublisher.propagateTransaction(transactionData, TransactionData.class.getName() + "Full Nodes");
        propagationPublisher.propagateTransaction(transactionData, TransactionData.class.getName() + "DSP Nodes");
        transactions.put(transactionData);
        transactionsToValidate.add(transactionData);
        transactionHelper.endHandleTransaction(transactionData);
        return "Received Transaction: " + transactionData.getHash();
    }

    @Scheduled(fixedRate = 1000)
    private void checkAttachedTransactions() { // TODO: start in different thread
        if (!isValidatorRunning.compareAndSet(false, true)) {
            return;
        }
        while (!transactionsToValidate.isEmpty()) {
            TransactionData transactionData = transactionsToValidate.remove();
            log.info("DSP Fully Checking transaction: {}", transactionData.getHash());
            DspVote dspVote = new DspVote();
            dspVote.transactionHash = transactionData.getHash();
            dspVote.isValidTransaction = validationService.fullValidation(transactionData);
            NodeCryptoHelper.setNodeHashAndSignature(dspVote); // TODO: Should sign the decision also
            sender.sendDspVote(dspVote);
        }
        isValidatorRunning.set(false);
    }

    @PostConstruct
    private void init() {
        transactionsToValidate = new PriorityQueue<>();
        isValidatorRunning = new AtomicBoolean(false);
    }

    public void handlePropagatedTransaction(TransactionData transactionData) {
        log.info("Received new propagated Address: {}", transactionData);
        if (!transactionHelper.startHandleTransaction(transactionData)) {
            log.info("Transaction already exists");
            return;
        }
        if (!transactionHelper.validateDataIntegrity(transactionData) ||
                !NodeCryptoHelper.verifyTransactionSignature(transactionData) ||
                !validationService.validatePow(transactionData)) {
            log.info("Data Integrity validation failed");
            return;
        }
        transactions.put(transactionData);
        propagationPublisher.propagateTransaction(transactionData, TransactionData.class.getName() + "Full Nodes");
        if (!transactionHelper.checkBalancesAndAddToPreBalance(transactionData.getBaseTransactions())) {
            transactionData.addSignature("Node ID", false); // TODO: replace with a sign mechanism
            sender.sendTransaction(transactionData);
        }
        transactionsToValidate.add(transactionData);
        transactionHelper.endHandleTransaction(transactionData);
    }
}