package io.coti.dspnode.services;

import io.coti.basenode.communication.interfaces.IPropagationPublisher;
import io.coti.basenode.communication.interfaces.ISender;
import io.coti.basenode.crypto.RejectedTransactionCrypto;
import io.coti.basenode.crypto.TransactionDspVoteCrypto;
import io.coti.basenode.data.*;
import io.coti.basenode.http.GetRejectedTransactionsResponse;
import io.coti.basenode.http.Response;
import io.coti.basenode.http.data.RejectedTransactionResponseData;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.model.RejectedTransactions;
import io.coti.basenode.services.BaseNodeEventService;
import io.coti.basenode.services.BaseNodeTransactionService;
import io.coti.basenode.services.interfaces.INetworkService;
import io.coti.basenode.services.interfaces.ITransactionHelper;
import io.coti.basenode.services.interfaces.IValidationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.STATUS_ERROR;
import static io.coti.basenode.http.BaseNodeHttpStringConstants.TRANSACTION_NONE_INDEXED_SERVER_ERROR;

@Slf4j
@Service
public class TransactionService extends BaseNodeTransactionService {

    public static final int NUMBER_OF_SECONDS_IN_DAY = 24 * 60 * 60;
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
    @Autowired
    private TransactionPropagationCheckService transactionPropagationCheckService;
    private BlockingQueue<TransactionData> transactionsToValidate;
    private Thread transactionValidationThread;
    @Autowired
    private RejectedTransactions rejectedTransactions;
    @Autowired
    private RejectedTransactionCrypto rejectedTransactionCrypto;
    @Autowired
    private BaseNodeEventService baseNodeEventService;

    @Override
    public void init() {
        transactionsToValidate = new PriorityBlockingQueue<>();
        transactionValidationThread = new Thread(this::checkAttachedTransactions, "DSP Validation");
        transactionValidationThread.start();
        super.init();
    }

    public void handleNewTransactionFromFullNode(TransactionData transactionData) {
        log.debug("Running new transaction from full node handler: {}", transactionData.getHash());
        AtomicBoolean isTransactionAlreadyPropagated = new AtomicBoolean(false);

        try {
            checkTransactionAlreadyPropagatedAndStartHandle(transactionData, isTransactionAlreadyPropagated);
            if (isTransactionAlreadyPropagated.get()) {
                log.debug("Transaction already exists: {}", transactionData.getHash());
                return;
            }
            if (rejectedTransactions.getByHash(transactionData.getHash()) != null) {
                log.debug("Transaction already rejected as invalid: {}", transactionData.getHash());
                notifyNodesOnRejectedTransaction(rejectedTransactions.getByHash(transactionData.getHash()));
                return;
            }
            if (!validationService.validatePropagatedTransactionDataIntegrity(transactionData)) {
                log.error("Data Integrity validation failed: {}", transactionData.getHash());
                propagateRejectedTransactionToFullNode(transactionData, RejectedTransactionDataReason.DATA_INTEGRITY);
                return;
            }
            if (hasOneOfParentsRejected(transactionData)) {
                log.debug("Transaction rejected as parent already rejected: {}", transactionData.getHash());
                propagateRejectedTransactionToFullNode(transactionData, RejectedTransactionDataReason.REJECTED_PARENT);
                return;
            }
            if (hasOneOfParentsMissing(transactionData)) {
                postponedTransactionMap.putIfAbsent(transactionData, true);
                return;
            }
            if (!validateAndAttachTransaction(transactionData)) {
                return;
            }
            propagationPublisher.propagate(transactionData, Arrays.asList(
                    NodeType.FullNode,
                    NodeType.TrustScoreNode,
                    NodeType.DspNode,
                    NodeType.ZeroSpendServer,
                    NodeType.FinancialServer,
                    NodeType.HistoryNode));
            transactionPropagationCheckService.addNewUnconfirmedTransaction(transactionData.getHash());
            transactionHelper.setTransactionStateToFinished(transactionData);
            transactionsToValidate.add(transactionData);
        } catch (Exception ex) {
            log.error("Exception while handling transaction {}", transactionData, ex);
        } finally {
            if (!isTransactionAlreadyPropagated.get()) {
                boolean isTransactionFinished = transactionHelper.isTransactionFinished(transactionData);
                transactionHelper.endHandleTransaction(transactionData);
                if (isTransactionFinished) {
                    processPostponedTransactions(transactionData);
                } else {
                    processPostponedRejectedTransactions(transactionData);
                }
            }
        }
    }

    @Override
    protected void propagateRejectedTransactionToFullNode(TransactionData transactionData,
                                                          RejectedTransactionDataReason rejectedTransactionDataReason) {
        if (baseNodeEventService.eventHappened(Event.TRUST_SCORE_CONSENSUS)) {
            log.debug("Informing full node that transaction is rejected");
            RejectedTransactionData rejectedTransactionData = new RejectedTransactionData(transactionData);
            rejectedTransactionData.setRejectionReason(rejectedTransactionDataReason);
            rejectedTransactionCrypto.signMessage(rejectedTransactionData);
            notifyNodesOnRejectedTransaction(rejectedTransactionData);
            rejectedTransactions.put(rejectedTransactionData);
        }
    }

    private void notifyNodesOnRejectedTransaction(RejectedTransactionData rejectedTransactionData) {
        propagationPublisher.propagate(rejectedTransactionData, Arrays.asList(NodeType.FullNode));
    }

    private boolean hasOneOfParentsRejected(TransactionData transactionData) {
        return (transactionData.getLeftParentHash() != null && rejectedTransactions.getByHash(transactionData.getLeftParentHash()) != null) ||
                (transactionData.getRightParentHash() != null && rejectedTransactions.getByHash(transactionData.getRightParentHash()) != null);
    }

    @Override
    protected void handlePostponedTransaction(TransactionData postponedTransaction, boolean isTransactionFromFullNode) {
        if (isTransactionFromFullNode) {
            handleNewTransactionFromFullNode(postponedTransaction);
        } else {
            handlePropagatedTransaction(postponedTransaction);
        }
    }

    @Scheduled(initialDelay = 1000, fixedDelay = 30000)
    private void clearRejectedTransactions() {
        rejectedTransactions.forEach(rejectedTransaction -> {
                    if (rejectedTransaction != null && (Instant.now().getEpochSecond() - rejectedTransaction.getRejectionTime().getEpochSecond() > NUMBER_OF_SECONDS_IN_DAY)) {
                        log.debug("removing rejected transaction due to TTL. hash: {}, rejection time: {}, reason: {}",
                                rejectedTransaction.getHash(), rejectedTransaction.getRejectionTime(), rejectedTransaction.getRejectionReason());
                        rejectedTransactions.delete(rejectedTransaction);
                    }
                }
        );
    }

    private void processPostponedRejectedTransactions(TransactionData rejectedTransaction) {
        if (postponedTransactionMap.size() > 0) {
            Map<TransactionData, Boolean> postponedChildrenTransactionMap = getPostponedChildrenTransactionMap(rejectedTransaction.getHash(), postponedTransactionMap);
            for (Map.Entry<TransactionData, Boolean> entry : postponedChildrenTransactionMap.entrySet()) {
                if (entry.getValue().equals(true)) {
                    propagateRejectedTransactionToFullNode(entry.getKey(), RejectedTransactionDataReason.REJECTED_PARENT);
                    postponedTransactionMap.remove(entry.getKey());
                    processPostponedTransactions(entry.getKey());
                }
            }
        }
    }

    @Override
    public ResponseEntity<IResponse> getRejectedTransactions() {
        try {
            List<RejectedTransactionResponseData> rejectedTransactionDataList = new ArrayList<>();
            rejectedTransactions.forEach(rejectedTransaction -> rejectedTransactionDataList.add(new RejectedTransactionResponseData(rejectedTransaction)));
            return ResponseEntity.ok(new GetRejectedTransactionsResponse(rejectedTransactionDataList));
        } catch (Exception e) {
            log.info("Exception while getting rejected transactions", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response(
                            TRANSACTION_NONE_INDEXED_SERVER_ERROR,
                            STATUS_ERROR));
        }
    }

    @Override
    public long getRejectedTransactionsSize() {
        return rejectedTransactions.size();
    }

    private void checkAttachedTransactions() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                TransactionData transactionData = transactionsToValidate.take();
                log.debug("DSP Fully Checking transaction: {}", transactionData.getHash());
                dspValidation(transactionData);
            } catch (InterruptedException e) {
                log.info("Dsp validation interrupted");
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                log.error("Dsp validation error", e);
            }
        }
        List<TransactionData> remainingValidations = new LinkedList<>();
        transactionsToValidate.drainTo(remainingValidations);
        if (!remainingValidations.isEmpty()) {
            log.info("Please wait for dsp validation of {} remaining transactions", remainingValidations.size());
            remainingValidations.forEach(transactionData -> {
                try {
                    dspValidation(transactionData);
                } catch (Exception e) {
                    log.error("Dsp validation error", e);
                }
            });
        }
    }

    private void dspValidation(TransactionData transactionData) {
        TransactionDspVote transactionDspVote = new TransactionDspVote(
                transactionData.getHash(),
                validationService.fullValidation(transactionData));
        transactionDspVoteCrypto.signMessage(transactionDspVote);
        NetworkNodeData zeroSpendServer = networkService.getSingleNodeData(NodeType.ZeroSpendServer);
        if (zeroSpendServer != null) {
            String zeroSpendReceivingAddress = zeroSpendServer.getReceivingFullAddress();
            log.debug("Sending DSP vote to {} for transaction {}", zeroSpendReceivingAddress, transactionData.getHash());
            sender.send(transactionDspVote, zeroSpendReceivingAddress);
        } else {
            log.error("ZeroSpendServer is not in the network. Failed to send dsp vote for transaction {}", transactionData.getHash());
        }
        transactionPropagationCheckService.addUnconfirmedTransactionDSPVote(transactionDspVote);
    }

    @Override
    protected void continueHandlePropagatedTransaction(TransactionData transactionData) {
        propagationPublisher.propagate(transactionData, Collections.singletonList(NodeType.FullNode));
        if (!EnumSet.of(TransactionType.ZeroSpend, TransactionType.Initial, TransactionType.EventHardFork).contains(transactionData.getType())) {
            transactionsToValidate.add(transactionData);
            transactionPropagationCheckService.addPropagatedUnconfirmedTransaction(transactionData.getHash());
        }

    }

    @Override
    protected void continueHandleMissingTransaction(TransactionData transactionData) {
        log.debug("Propagate missing transaction {} by {} to {}", transactionData.getHash(), NodeType.DspNode, NodeType.FullNode);
        propagationPublisher.propagate(transactionData, Collections.singletonList(NodeType.FullNode));
    }

    public void shutdown() {
        log.info("Shutting down {}", this.getClass().getSimpleName());
        transactionValidationThread.interrupt();
        try {
            transactionValidationThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Interrupted shutdown {}", this.getClass().getSimpleName());
        }
    }
}
