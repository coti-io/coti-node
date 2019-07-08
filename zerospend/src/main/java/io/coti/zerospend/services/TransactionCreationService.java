package io.coti.zerospend.services;


import io.coti.basenode.communication.interfaces.IPropagationPublisher;
import io.coti.basenode.crypto.NodeCryptoHelper;
import io.coti.basenode.crypto.TransactionCrypto;
import io.coti.basenode.data.*;
import io.coti.basenode.services.TransactionHelper;
import io.coti.basenode.services.interfaces.IValidationService;
import io.coti.zerospend.crypto.TransactionCryptoCreator;
import io.coti.zerospend.data.ZeroSpendTransactionType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

import static io.coti.zerospend.data.ZeroSpendTransactionType.GENESIS;
import static io.coti.zerospend.data.ZeroSpendTransactionType.STARVATION;

@Slf4j
@Service
public class TransactionCreationService {
    private static final int ZERO_SPEND_ADDRESS_INDEX = 0;
    @Autowired
    private TransactionHelper transactionHelper;
    @Autowired
    private IValidationService validationService;
    @Autowired
    private IPropagationPublisher propagationPublisher;
    @Autowired
    private TransactionCrypto transactionCrypto;
    @Autowired
    private TransactionCryptoCreator transactionCryptoCreator;
    @Autowired
    private DspVoteService dspVoteService;
    @Autowired
    private NodeCryptoHelper nodeCryptoHelper;
    @Value("${zerospend.seed}")
    private String seed;

    public String createNewStarvationZeroSpendTransaction(TransactionData transactionData) {
        return createNewZeroSpendTransaction(transactionData, STARVATION);
    }

    public void createNewGenesisZeroSpendTransaction(double trustscore) {
        createZeroSpendTransaction(trustscore, GENESIS);
    }

    public String createNewZeroSpendTransaction(TransactionData incomingTransactionData, ZeroSpendTransactionType zeroSpendTransactionType) {
        if (!validationService.fullValidation(incomingTransactionData)) {
            log.error("Validation for waiting source  failed! requesting transaction {}", incomingTransactionData);
            return "Invalid";
        }
        TransactionData zeroSpendTransaction = createZeroSpendTransaction(incomingTransactionData, zeroSpendTransactionType);
        sendTransactionToPublisher(zeroSpendTransaction);
        return "Ok";
    }

    private TransactionData createZeroSpendTransaction(TransactionData existingTransactionData, ZeroSpendTransactionType zeroSpendTransactionType) {
        log.debug("Creating a new Zero Spend Transaction for transaction : Hash = {} , SenderTrustScore = {}", existingTransactionData.getHash(), existingTransactionData.getSenderTrustScore());
        TransactionData transactionData = createZeroSpendTransactionData(existingTransactionData.getSenderTrustScore(), zeroSpendTransactionType);

        if (zeroSpendTransactionType == STARVATION) {
            transactionData.setLeftParentHash(existingTransactionData.getHash());
        }

        attachTransactionToCluster(transactionData, zeroSpendTransactionType);
        return transactionData;
    }

    private TransactionData createZeroSpendTransaction(double trustScore, ZeroSpendTransactionType zeroSpendTransactionType) {
        TransactionData transactionData = createZeroSpendTransactionData(trustScore, zeroSpendTransactionType);

        attachTransactionToCluster(transactionData, zeroSpendTransactionType);
        sendTransactionToPublisher(transactionData);
        return transactionData;
    }

    private void attachTransactionToCluster(TransactionData transactionData, ZeroSpendTransactionType zeroSpendTransactionType) {
        DspConsensusResult dspConsensusResult = new DspConsensusResult(transactionData.getHash());
        dspConsensusResult.setDspConsensus(true);
        dspVoteService.setIndexForDspResult(transactionData, dspConsensusResult);
        transactionHelper.attachTransactionToCluster(transactionData);
        log.info("Created a new {} Zero Spend Transaction: Hash = {} , SenderTrustScore = {} ", zeroSpendTransactionType, transactionData.getHash(), transactionData.getSenderTrustScore());
    }

    private void sendTransactionToPublisher(TransactionData transactionData) {
        log.debug("Sending Zero Spend Transaction. transaction: Hash = {} , SenderTrustScore = {}", transactionData.getHash(), transactionData.getSenderTrustScore());
        propagationPublisher.propagate(transactionData, Arrays.asList(NodeType.DspNode, NodeType.TrustScoreNode, NodeType.FinancialServer, NodeType.HistoryNode));

    }

    public void createGenesisTransactions() {
        log.info("Creating genesis transactions");
        for (int trustScore = 0; trustScore <= 100; trustScore = trustScore + 10) {
            TransactionData transactionData = createZeroSpendTransactionData(trustScore, GENESIS);

            DspConsensusResult dspConsensusResult = new DspConsensusResult(transactionData.getHash());
            dspConsensusResult.setDspConsensus(true);
            dspVoteService.setIndexForDspResult(transactionData, dspConsensusResult);

            transactionHelper.attachTransactionToCluster(transactionData);
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private TransactionData createZeroSpendTransactionData(double trustScore, ZeroSpendTransactionType description) {
        Map<Hash, Integer> addressHashToAddressIndexMap = new HashMap<>();
        List<BaseTransactionData> baseTransactions = new ArrayList<>();
        Hash addressHash = nodeCryptoHelper.generateAddress(seed, ZERO_SPEND_ADDRESS_INDEX);
        BaseTransactionData baseTransactionData = new InputBaseTransactionData(addressHash, BigDecimal.ZERO, Instant.now());
        addressHashToAddressIndexMap.put(addressHash, ZERO_SPEND_ADDRESS_INDEX);
        baseTransactions.add(baseTransactionData);
        TransactionData transactionData = new TransactionData(baseTransactions, description.name(), trustScore, Instant.now(), TransactionType.ZeroSpend);
        transactionData.setAttachmentTime(Instant.now());


        transactionCryptoCreator.signBaseTransactions(transactionData, addressHashToAddressIndexMap);
        transactionCrypto.signMessage(transactionData);
        return transactionData;
    }
}