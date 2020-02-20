package io.coti.dspnode.services;

import io.coti.basenode.communication.interfaces.IPropagationPublisher;
import io.coti.basenode.communication.interfaces.ISender;
import io.coti.basenode.crypto.TransactionDspVoteCrypto;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.data.TransactionDspVote;
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
import java.util.EnumSet;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
public class TransactionService extends BaseNodeTransactionService {

    private Queue<TransactionData> transactionsToVotePositive;
    private Queue<TransactionData> transactionsToVoteNegative;
    private Queue<TransactionData> transactionsToPropagateFromMissing;
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
    private TransactionDspVoteCrypto transactionDspVoteCrypto;
    @Autowired
    private INetworkService networkService;

    @Override
    public void init() {
        transactionsToVotePositive = new PriorityQueue<>();
        transactionsToVoteNegative = new PriorityQueue<>();
        transactionsToPropagateFromMissing = new PriorityQueue<>();
        isValidatorRunning = new AtomicBoolean(false);
        super.init();
    }

    @Override
    public void handleNewTransactionFromFullNode(TransactionData transactionData) {
        try {
            log.debug("Running new transactions from full node handler");
            if (transactionHelper.isTransactionAlreadyPropagated(transactionData)) {
                log.debug("Transaction already exists: {}", transactionData.getHash());
                return;
            }
            transactionHelper.startHandleTransaction(transactionData);
            if (!validationService.validatePropagatedTransactionIntegrityPhase1(transactionData)) {
                log.info("Invalid Transaction Received!");
                return;
            }
            if (hasOneOfParentsMissing(transactionData)) {
                if (!postponedTransactions.containsKey(transactionData)) {
                    postponedTransactions.put(transactionData, true);
                }
                return;
            }
            transactionHelper.attachTransactionToCluster(transactionData);
            transactionHelper.setTransactionStateToSaved(transactionData);
            propagationPublisher.propagate(transactionData, Arrays.asList(
                    NodeType.FullNode,
                    NodeType.TrustScoreNode,
                    NodeType.DspNode,
                    NodeType.ZeroSpendServer,
                    NodeType.FinancialServer,
                    NodeType.HistoryNode));
            transactionPropagationCheckService.addUnconfirmedTransaction(transactionData.getHash());
            if (validationService.validatePropagatedTransactionIntegrityPhase2(transactionData) &&
                    validationService.validateBalancesAndAddToPreBalance(transactionData)) {
                transactionsToVotePositive.add(transactionData);
            } else {
                transactionsToVoteNegative.add(transactionData);
            }
            transactionHelper.setTransactionStateToFinished(transactionData);
        } catch (Exception ex) {
            log.error("Exception while handling transaction {}", transactionData, ex);
        } finally {
            boolean isTransactionFinished = transactionHelper.isTransactionFinished(transactionData);
            transactionHelper.endHandleTransaction(transactionData);
            if (isTransactionFinished) {
                processPostponedTransactions(transactionData);
            }
        }
    }

    @Scheduled(fixedRate = 1000)
    private void processTransactionsVotes() {
        if (!isValidatorRunning.compareAndSet(false, true)) {
            return;
        }
        while (!transactionsToVotePositive.isEmpty()) {
            TransactionData transactionData = transactionsToVotePositive.remove();
            log.debug("DSP Fully Checking transaction: {}", transactionData.getHash());
            TransactionDspVote transactionDspVote = new TransactionDspVote(
                    transactionData.getHash(),
                    true);
            transactionDspVoteCrypto.signMessage(transactionDspVote);
            String zerospendReceivingAddress = networkService.getSingleNodeData(NodeType.ZeroSpendServer).getReceivingFullAddress();
            log.debug("Sending DSP vote to {} for transaction {}", zerospendReceivingAddress, transactionData.getHash());
            sender.send(transactionDspVote, zerospendReceivingAddress);
        }
        while (!transactionsToVoteNegative.isEmpty()) {
            TransactionData transactionData = transactionsToVoteNegative.remove();
            log.debug("DSP incorrect transaction: {}", transactionData.getHash());
            TransactionDspVote transactionDspVote = new TransactionDspVote(
                    transactionData.getHash(),
                    false);
            transactionDspVoteCrypto.signMessage(transactionDspVote);
            String zerospendReceivingAddress = networkService.getSingleNodeData(NodeType.ZeroSpendServer).getReceivingFullAddress();
            log.debug("Sending DSP negative vote to {} for transaction {}", zerospendReceivingAddress, transactionData.getHash());
            sender.send(transactionDspVote, zerospendReceivingAddress);
        }
        isValidatorRunning.set(false);
    }

    @Override
    protected void continueHandlePropagatedTransaction(TransactionData transactionData, boolean opinionOnTheTransaction) {
        propagationPublisher.propagate(transactionData, Arrays.asList(NodeType.FullNode));
        if (!EnumSet.of(TransactionType.ZeroSpend, TransactionType.Initial).contains(transactionData.getType())) {
            if (opinionOnTheTransaction) {
                transactionsToVotePositive.add(transactionData);
            } else {
                transactionsToVoteNegative.add(transactionData);
            }
        }

    }

    @Override
    protected void propagateMissingTransaction(TransactionData transactionData) {
        transactionsToPropagateFromMissing.add(transactionData);
    }

    @Override
    public void delayedMissingTransactionsPropagation() {
        Thread delayedTransactionPropagationThread = delayedMissingTransactionPropagationThread();

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        delayedTransactionPropagationThread.start();

    }

    private Thread delayedMissingTransactionPropagationThread() {
        return new Thread(() -> {
            while (!transactionsToPropagateFromMissing.isEmpty()) {
                TransactionData transactionData = transactionsToPropagateFromMissing.remove();
                log.debug("Propagate missing transaction {} by {} to {}", transactionData.getHash(), NodeType.DspNode, NodeType.FullNode);
                propagationPublisher.propagate(transactionData, Arrays.asList(NodeType.FullNode));
            }
        });
    }

}