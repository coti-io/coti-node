package io.coti.dspnode.services;

import io.coti.basenode.communication.interfaces.IPropagationPublisher;
import io.coti.basenode.communication.interfaces.ISender;
import io.coti.basenode.crypto.DspVoteCrypto;
import io.coti.basenode.data.DspVote;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.services.BaseNodeTransactionService;
import io.coti.basenode.services.interfaces.ITransactionHelper;
import io.coti.basenode.services.interfaces.IValidationService;
import io.coti.dspnode.data.NotTotalConfirmedTransactionHash;
import io.coti.dspnode.model.NotTotalConfirmedTransactionHashes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
public class TransactionService extends BaseNodeTransactionService {
    Queue<TransactionData> transactionsToValidate;
    AtomicBoolean isValidatorRunning;
    @Value("${zerospend.receiving.address}")
    private String zerospendReceivingAddress;

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
    private ClusterStampService clusterStampService;
    @Autowired
    private NotTotalConfirmedTransactionHashes notTotalConfirmedTransactionHashes;

    public String handleNewTransactionFromFullNode(TransactionData transactionData) {
        if(clusterStampService.isReadyForClusterStamp()){
            return "Waiting for cluster stamp";
        }
        try {
            log.debug("Running new transactions from full node handler");
            if (transactionHelper.isTransactionAlreadyPropagated(transactionData)) {
                log.debug("Transaction already exists: {}", transactionData.getHash());
                return "Transaction Exists: " + transactionData.getHash();
            }
            transactionHelper.startHandleTransaction(transactionData);
            if (!validationService.validatePropagatedTransactionDataIntegrity(transactionData) ||
                    !validationService.validateBalancesAndAddToPreBalance(transactionData)) {
                log.info("Invalid Transaction Received!");
                return "Invalid Transaction Received: " + transactionData.getHash();
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
            notTotalConfirmedTransactionHashes.put(new NotTotalConfirmedTransactionHash(transactionData.getHash()));
            return "Received Transaction: " + transactionData.getHash();
        } finally {
            transactionHelper.endHandleTransaction(transactionData);
        }
    }

    @Scheduled(fixedRate = 1000)
    private void checkAttachedTransactions() {
        if (!isValidatorRunning.compareAndSet(false, true)) {
            return;
        }
        while (!clusterStampService.isReadyForClusterStamp() && !transactionsToValidate.isEmpty()) {
            TransactionData transactionData = transactionsToValidate.remove();
            log.debug("DSP Fully Checking transaction: {}", transactionData.getHash());
            DspVote dspVote = new DspVote(
                    transactionData.getHash(),
                    validationService.fullValidation(transactionData));
            dspVoteCrypto.signMessage(dspVote);
            log.debug("Sending DSP vote to {} for transaction {}", zerospendReceivingAddress, transactionData.getHash());
            sender.send(dspVote, zerospendReceivingAddress);
        }
        isValidatorRunning.set(false);
    }

    @Override
    public void init() {
        transactionsToValidate = new PriorityQueue<>();
        isValidatorRunning = new AtomicBoolean(false);
        super.init();
    }

    public void continueHandlePropagatedTransaction(TransactionData transactionData) {

        propagationPublisher.propagate(transactionData, Arrays.asList(NodeType.FullNode));
        if (!transactionData.isZeroSpend()) {
            notTotalConfirmedTransactionHashes.put(new NotTotalConfirmedTransactionHash(transactionData.getHash()));
            transactionsToValidate.add(transactionData);
        }
    }
}