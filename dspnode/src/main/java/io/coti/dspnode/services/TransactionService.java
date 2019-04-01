package io.coti.dspnode.services;

import io.coti.basenode.communication.interfaces.IPropagationPublisher;
import io.coti.basenode.communication.interfaces.ISender;
import io.coti.basenode.crypto.DspVoteCrypto;
import io.coti.basenode.data.DspVote;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.data.TransactionType;
import io.coti.basenode.services.BaseNodeTransactionService;
import io.coti.basenode.services.interfaces.INetworkService;
import io.coti.basenode.services.interfaces.ITransactionHelper;
import io.coti.basenode.services.interfaces.IValidationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
public class TransactionService extends BaseNodeTransactionService {
    private Queue<TransactionData> transactionsToValidate;
    private AtomicBoolean isValidatorRunning;

    @Autowired
    private ITransactionHelper transactionHelper;
    @Autowired
    private IPropagationPublisher propagationPublisher;
    @Autowired
    private IValidationService validationService;
    @Autowired
    private ISender sender;
    @Autowired
    private DspVoteCrypto dspVoteCrypto;
    @Autowired
    private INetworkService networkService;

    @Override
    public void init() {
        transactionsToValidate = new PriorityQueue<>();
        isValidatorRunning = new AtomicBoolean(false);
        super.init();
        Thread thread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(5000);
                    log.info("Waiting transactions size: {}, postponed size: {}", parentProcessingTransactions.size(), postponedTransactions.size());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

        });
        thread.start();
    }

    @Scheduled(fixedDelay = 5000)
    public void checkWaitingTransactions() {
        log.info("Waiting transactions size: {}", parentProcessingTransactions.size());
    }

    public void handleNewTransactionFromFullNode(TransactionData transactionData) {
        try {
            log.debug("Running new transactions from full node handler");
            if (transactionHelper.isTransactionAlreadyPropagated(transactionData)) {
                log.debug("Transaction already exists: {}", transactionData.getHash());
                return;
            }
            transactionHelper.startHandleTransaction(transactionData);
            if (!validationService.validatePropagatedTransactionDataIntegrity(transactionData) ||
                    !validationService.validateBalancesAndAddToPreBalance(transactionData)) {
                log.info("Invalid Transaction Received!");
                return;
            }
            transactionHelper.attachTransactionToCluster(transactionData);
            transactionHelper.setTransactionStateToSaved(transactionData);
            propagationPublisher.propagate(transactionData, Arrays.asList(
                    NodeType.FullNode,
                    NodeType.TrustScoreNode,
                    NodeType.DspNode,
                    NodeType.ZeroSpendServer,
                    NodeType.FinancialServer));
            transactionHelper.setTransactionStateToFinished(transactionData);
            transactionsToValidate.add(transactionData);
        } catch (Exception ex) {
            log.error("Exception while handling transaction {}", transactionData, ex);
        } finally {
            transactionHelper.endHandleTransaction(transactionData);
        }
    }

    @Scheduled(fixedRate = 1000)
    private void checkAttachedTransactions() {
        if (!isValidatorRunning.compareAndSet(false, true)) {
            return;
        }
        while (!transactionsToValidate.isEmpty()) {
            TransactionData transactionData = transactionsToValidate.remove();
            log.debug("DSP Fully Checking transaction: {}", transactionData.getHash());
            DspVote dspVote = new DspVote(
                    transactionData.getHash(),
                    validationService.fullValidation(transactionData));
            dspVoteCrypto.signMessage(dspVote);
            String zerospendReceivingAddress = networkService.getSingleNodeData(NodeType.ZeroSpendServer).getReceivingFullAddress();
            log.debug("Sending DSP vote to {} for transaction {}", zerospendReceivingAddress, transactionData.getHash());
            sender.send(dspVote, zerospendReceivingAddress);
        }
        isValidatorRunning.set(false);
    }

    public void continueHandlePropagatedTransaction(TransactionData transactionData) {
        propagationPublisher.propagate(transactionData, Arrays.asList(NodeType.FullNode));
        if (!transactionData.getType().equals(TransactionType.ZeroSpend)) {
            transactionsToValidate.add(transactionData);
        }

    }
}