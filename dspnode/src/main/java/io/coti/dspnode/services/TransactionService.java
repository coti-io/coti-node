package io.coti.dspnode.services;

import io.coti.basenode.communication.interfaces.IPropagationPublisher;
import io.coti.basenode.communication.interfaces.ISender;
import io.coti.basenode.crypto.DspVoteCrypto;
import io.coti.basenode.crypto.TransactionCrypto;
import io.coti.basenode.data.DspVote;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.services.BaseNodeTransactionService;
import io.coti.basenode.services.interfaces.ITransactionHelper;
import io.coti.basenode.services.interfaces.IValidationService;
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
    private TransactionCrypto transactionCrypto;
    @Autowired
    private IPropagationPublisher propagationPublisher;
    @Autowired
    private IValidationService validationService;
    @Autowired
    private ISender sender;
    @Autowired
    private DspVoteCrypto dspVoteCrypto;

    public String handleNewTransactionFromFullNode(TransactionData transactionData) {
        log.debug("Running new transactions from full node handler");
        if (!transactionHelper.startHandleTransaction(transactionData)) {
            log.debug("Transaction already exists");
            return "Transaction Exists: " + transactionData.getHash();
        }
        if (!transactionHelper.validateTransaction(transactionData) ||
                !transactionCrypto.verifySignature(transactionData) ||
                !validationService.validatePow(transactionData) ||
                !transactionHelper.checkBalancesAndAddToPreBalance(transactionData)) {
            log.info("Invalid Transaction Received!");
            return "Invalid Transaction Received: " + transactionData.getHash();
        }
        transactionHelper.attachTransactionToCluster(transactionData);
        transactionHelper.setTransactionStateToSaved(transactionData);
        propagationPublisher.propagate(transactionData, Arrays.asList(
                NodeType.FullNode,
                NodeType.TrustScoreNode,
                NodeType.DspNode,
                NodeType.ZeroSpendServer));
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
            log.debug("DSP Fully Checking transaction: {}", transactionData.getHash());
            DspVote dspVote = new DspVote(
                    transactionData.getHash(),
                    validationService.fullValidation(transactionData));
            dspVoteCrypto.signMessage(dspVote);
            log.debug("Sending DSP vote to: {}", zerospendReceivingAddress);
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
        transactionsToValidate.add(transactionData);
    }
}