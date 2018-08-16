package io.coti.dspnode.services;

import io.coti.common.communication.interfaces.IPropagationPublisher;
import io.coti.common.communication.interfaces.ISender;
import io.coti.common.crypto.DspVoteCrypto;
import io.coti.common.crypto.TransactionCrypto;
import io.coti.common.data.DspConsensusResult;
import io.coti.common.data.DspVote;
import io.coti.common.data.TransactionData;
import io.coti.common.model.Transactions;
import io.coti.common.services.interfaces.IBalanceService;
import io.coti.common.services.interfaces.ITransactionHelper;
import io.coti.common.services.interfaces.IValidationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
public class TransactionService {
    Queue<TransactionData> transactionsToValidate;
    AtomicBoolean isValidatorRunning;
    @Value("#{'${zerospend.receiving.address}'.split(',')}")
    private String zerospendReceivingAddress;

    @Autowired
    private ITransactionHelper transactionHelper;
    @Autowired
    private TransactionCrypto transactionCrypto;
    @Autowired
    private IPropagationPublisher propagationPublisher;
    @Autowired
    private IValidationService validationService;
    @Autowired
    private IBalanceService balanceService;
    @Autowired
    private Transactions transactions;
    @Autowired
    private ISender sender;
    @Autowired
    private DspVoteCrypto dspVoteCrypto;

    public String handleNewTransactionFromFullNode(TransactionData transactionData) {
        log.info("Running new transactions from full node handler");
        if (!transactionHelper.startHandleTransaction(transactionData)) {
            log.info("Transaction already exists");
            return "Transaction Exists: " + transactionData.getHash();
        }
        transactions.put(transactionData);
        transactionHelper.setTransactionStateToSaved(transactionData);
        if (!transactionHelper.validateTransaction(transactionData) ||
                !transactionCrypto.verifySignature(transactionData) ||
                !validationService.validatePoft(transactionData) ||
                !transactionHelper.checkBalancesAndAddToPreBalance(transactionData)) {
            log.info("Invalid Transaction Received!");
            return "Invalid Transaction Received: " + transactionData.getHash();
        }
        propagationPublisher.propagate(transactionData, TransactionData.class.getName() + "ZeroSpend Server");
        propagationPublisher.propagate(transactionData, TransactionData.class.getName() + "Full Nodes");
        propagationPublisher.propagate(transactionData, TransactionData.class.getName() + "DSP Nodes");
        propagationPublisher.propagate(transactionData, TransactionData.class.getName() + "TrustScore Nodes");
        transactionHelper.attachTransactionToCluster(transactionData);
        transactionHelper.setTransactionStateToFinished(transactionData);
        transactionsToValidate.add(transactionData);

        transactionHelper.endHandleTransaction(transactionData);
        return "Received Transaction: " + transactionData.getHash();
    }

    @Scheduled(fixedRate = 1000)
    private void checkAttachedTransactions() {
        if (!isValidatorRunning.compareAndSet(false, true)) {
            return;
        }
        while (!transactionsToValidate.isEmpty()) {
            TransactionData transactionData = transactionsToValidate.remove();
            log.info("DSP Fully Checking transaction: {}", transactionData.getHash());
            DspVote dspVote = new DspVote(
                    transactionData.getHash(),
                    validationService.fullValidation(transactionData));
            dspVoteCrypto.signMessage(dspVote);
            log.info("Sending DSP vote to: {}", zerospendReceivingAddress);
            sender.send(dspVote, zerospendReceivingAddress);
        }
        isValidatorRunning.set(false);
    }

    @PostConstruct
    private void init() {
        transactionsToValidate = new PriorityQueue<>();
        isValidatorRunning = new AtomicBoolean(false);
    }

    public void handlePropagatedTransaction(TransactionData transactionData) {
        try {
            log.info("DSP Propagated Transaction received: {}", transactionData.getHash().toHexString());
            if (!transactionHelper.startHandleTransaction(transactionData)) {
                log.info("Transaction already exists");
                return;
            }
            if (!transactionHelper.validateTransaction(transactionData) ||
                    !transactionCrypto.verifySignature(transactionData) ||
                    !validationService.validatePoft(transactionData)) {
                log.info("Data Integrity validation failed");
                return;
            }
            boolean checkBalancesAndAddToPreBalance = transactionHelper.checkBalancesAndAddToPreBalance(transactionData);
            if (!checkBalancesAndAddToPreBalance) {
                log.info("Balances check failed for transaction: {}", transactionData.getHash());
                return;
            }
            transactions.put(transactionData);
            transactionHelper.setTransactionStateToSaved(transactionData);
            propagationPublisher.propagate(transactionData, TransactionData.class.getName() + "Full Nodes");
            transactionHelper.setTransactionStateToFinished(transactionData);
            transactionsToValidate.add(transactionData);
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            transactionHelper.endHandleTransaction(transactionData);
        }
    }

    public void handleVoteConclusion(DspConsensusResult dspConsensusResult) {
        log.info("Received DspConsensus result: " + dspConsensusResult.getHash());
        if (!transactionHelper.handleVoteConclusionResult(dspConsensusResult)) {
            log.error("Illegal vote received: " + dspConsensusResult.getHash());
        } else {
            balanceService.setDspcToTrue(dspConsensusResult);
            propagationPublisher.propagate(dspConsensusResult, DspConsensusResult.class.getName() + "Full Nodes");
        }
    }
}