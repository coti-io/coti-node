package io.coti.zerospend.services;


import io.coti.basenode.communication.interfaces.IPropagationPublisher;
import io.coti.basenode.crypto.NodeCryptoHelper;
import io.coti.basenode.crypto.TransactionCrypto;
import io.coti.basenode.data.*;
import io.coti.basenode.services.TransactionHelper;
import io.coti.basenode.services.TransactionIndexService;
import io.coti.basenode.services.interfaces.IValidationService;
import io.coti.zerospend.crypto.BaseTransactionWithPrivateKey;
import io.coti.zerospend.crypto.TransactionCryptoCreator;
import io.coti.zerospend.data.ZeroSpendTransactionType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

import static io.coti.zerospend.data.ZeroSpendTransactionType.GENESIS;
import static io.coti.zerospend.data.ZeroSpendTransactionType.STARVATION;

@Slf4j
@Service
public class ZeroSpendTransactionCreationService {
    private final BigDecimal zeroSpendAmount = new BigDecimal(0);
    @Value("${global.private.key}")
    private String zeroSpendGlobalPrivateKey;
    @Autowired
    private TransactionIndexService transactionIndexService;
    @Autowired
    private TransactionHelper transactionHelper;
    @Autowired
    private IValidationService validationService;
    @Autowired
    private IPropagationPublisher propagationPublisher;
    @Autowired
    private TransactionCrypto transactionCrypto;

    public String createNewStarvationZeroSpendTransaction(TransactionData transactionData) {
        return createNewZeroSpendTransaction(transactionData, STARVATION);
    }

    public String createNewGenesisZeroSpendTransaction(ZeroSpendTransactionRequest zeroSpendTransactionRequest) {
        return createNewZeroSpendTransaction(zeroSpendTransactionRequest.getTransactionData(), GENESIS);
    }

    public String createNewZeroSpendTransaction(TransactionData incomingTransactionData, ZeroSpendTransactionType zeroSpendTransactionType) {
        log.info("Creating new Zero Spend transaction");
        if (!validationService.fullValidation(incomingTransactionData)) {
            log.error("Validation for waiting source  failed! requesting transaction {}", incomingTransactionData);
            return "Invalid";
        }
        TransactionData zeroSpendTransaction = createZeroSpendTransaction(incomingTransactionData, zeroSpendTransactionType);
        sendTransactionToPublisher(zeroSpendTransaction);
        return "Ok";
    }

    private TransactionData createZeroSpendTransaction(TransactionData existingTransactionData, ZeroSpendTransactionType zeroSpendTransactionType) {
        log.info("Creating a new Zero Spend Transaction for {}", existingTransactionData);
        TransactionData zeroSpendTransactionData =
                new TransactionData(
                        Arrays.asList(new BaseTransactionWithPrivateKey(zeroSpendAmount, new Date(), zeroSpendGlobalPrivateKey)),
                        null,
                        zeroSpendTransactionType.name(),
                        existingTransactionData.getSenderTrustScore(),
                        new Date());
        zeroSpendTransactionData.setZeroSpend(true);
        addSignature(zeroSpendTransactionData);
        zeroSpendTransactionData.setNodeHash(NodeCryptoHelper.getNodeHash());
        if (zeroSpendTransactionType == STARVATION) {
            zeroSpendTransactionData.setLeftParentHash(existingTransactionData.getHash());
        }
        zeroSpendTransactionData.setAttachmentTime(new Date());
        transactionCrypto.signMessage(zeroSpendTransactionData);
        transactionHelper.attachTransactionToCluster(zeroSpendTransactionData);
        log.info("Created a new Zero Spend Transaction: {}", zeroSpendTransactionData);
        return zeroSpendTransactionData;
    }

    private void addSignature(TransactionData transactionData) {
        TransactionCryptoCreator transactionCryptoCreator = new TransactionCryptoCreator(transactionData);
        transactionCryptoCreator.signTransaction();
    }

    private void sendTransactionToPublisher(TransactionData transactionData) {
        log.info("Sending Zero Spend Transaction to DSPs. transaction: {}  channel: {}", transactionData);
        propagationPublisher.propagate(transactionData, Arrays.asList(NodeType.DspNode, NodeType.TrustScoreNode));

    }

    public void createGenesisTransactions() {
        log.info("Creating genesis transactions");
        List<TransactionData> genesisTransactions = new LinkedList<>();
        int indexCounter = 0;
        for (int trustScore = 0; trustScore <= 100; trustScore = trustScore + 10) {
            TransactionData transactionData = createInitialZeroSpendTransaction(trustScore, "Genesis");

            DspConsensusResult dspConsensusResult = new DspConsensusResult(transactionData.getHash());
            dspConsensusResult.setIndex(indexCounter);
            dspConsensusResult.setDspConsensus(true);
            transactionData.setDspConsensusResult(dspConsensusResult);
            transactionData.setGenesis(true);

            genesisTransactions.add(transactionData);
            transactionHelper.attachTransactionToCluster(transactionData);
            transactionIndexService.insertNewTransactionIndex(transactionData);
            indexCounter++;
        }
    }

    private TransactionData createInitialZeroSpendTransaction(double trustScore, String description) {
        List<BaseTransactionData> baseTransactions = new ArrayList<>();
        BaseTransactionWithPrivateKey baseTransactionWithPrivateKey = new BaseTransactionWithPrivateKey(new BigDecimal(0), new Date(), zeroSpendGlobalPrivateKey);
        baseTransactions.add(baseTransactionWithPrivateKey);
        TransactionData transactionData = new TransactionData(baseTransactions, description, trustScore, new Date());
        transactionData.setAttachmentTime(new Date());
        transactionData.setZeroSpend(true);

        TransactionCryptoCreator transactionCryptoData = new TransactionCryptoCreator(transactionData);
        transactionCryptoData.signTransaction();
        transactionCrypto.signMessage(transactionData);
        return transactionData;
    }
}