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
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
public class TransactionService extends BaseNodeTransactionService {

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

    @Override
    public void init() {
        transactionsToValidate = new PriorityBlockingQueue<>();
        transactionValidationThread = new Thread(this::checkAttachedTransactions, "DSP Validation");
        transactionValidationThread.start();
        super.init();
    }

    public void handleNewTransactionFromFullNode(TransactionData transactionData) {
        log.debug("Running new transactions from full node handler");
        AtomicBoolean isTransactionAlreadyPropagated = new AtomicBoolean(false);

        try {
            checkTransactionAlreadyPropagatedAndStartHandle(transactionData, isTransactionAlreadyPropagated);
            if (isTransactionAlreadyPropagated.get()) {
                log.debug("Transaction already exists: {}", transactionData.getHash());
                return;
            }
            if (!validationService.validatePropagatedTransactionDataIntegrity(transactionData)) {
                log.error("Data Integrity validation failed: {}", transactionData.getHash());
                return;
            }
            if (hasOneOfParentsMissing(transactionData)) {
                if (!postponedTransactions.containsKey(transactionData)) {
                    postponedTransactions.put(transactionData, true);
                }
                return;
            }
            if (!validationService.validateBalancesAndAddToPreBalance(transactionData)) {
                log.error("Balance check failed: {}", transactionData.getHash());
                return;
            }
            if (transactionData.getType().equals(TransactionType.TokenGeneration) && !validationService.validateCurrencyUniquenessAndAddUnconfirmedRecord(transactionData)) {
                log.error("Not unique token generation attempt by transaction: {}", transactionData.getHash());
                return;
            }
            if (transactionData.getType().equals(TransactionType.TokenMinting) && !validationService.validateTokenMintingAndAddToAllocatedAmount(transactionData)) {
                log.error("Minting balance check failed: {}", transactionData.getHash());
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
                }
            }
        }
    }

    @Override
    protected void handlePostponedTransaction(TransactionData postponedTransaction, boolean isTransactionFromFullNode) {
        if (isTransactionFromFullNode) {
            handleNewTransactionFromFullNode(postponedTransaction);
        } else {
            handlePropagatedTransaction(postponedTransaction);
        }
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
        String zeroSpendReceivingAddress = networkService.getSingleNodeData(NodeType.ZeroSpendServer).getReceivingFullAddress();
        log.debug("Sending DSP vote to {} for transaction {}", zeroSpendReceivingAddress, transactionData.getHash());
        sender.send(transactionDspVote, zeroSpendReceivingAddress);
        transactionPropagationCheckService.addUnconfirmedTransactionDSPVote(transactionDspVote);
    }

    @Override
    protected void continueHandlePropagatedTransaction(TransactionData transactionData) {
        propagationPublisher.propagate(transactionData, Collections.singletonList(NodeType.FullNode));
        if (!EnumSet.of(TransactionType.ZeroSpend, TransactionType.Initial).contains(transactionData.getType())) {
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