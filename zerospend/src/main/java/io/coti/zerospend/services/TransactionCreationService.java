package io.coti.zerospend.services;


import io.coti.basenode.communication.interfaces.IPropagationPublisher;
import io.coti.basenode.crypto.TransactionCrypto;
import io.coti.basenode.data.*;
import io.coti.basenode.services.TransactionHelper;
import io.coti.basenode.services.TransactionIndexService;
import io.coti.basenode.services.interfaces.IValidationService;
import io.coti.zerospend.crypto.TransactionCryptoCreator;
import io.coti.zerospend.data.ZeroSpendTransactionType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static io.coti.zerospend.data.ZeroSpendTransactionType.GENESIS;
import static io.coti.zerospend.data.ZeroSpendTransactionType.STARVATION;

@Slf4j
@Service
public class TransactionCreationService {
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
    @Autowired
    private TransactionCryptoCreator transactionCryptoCreator;
    @Autowired
    private DspVoteService dspVoteService;

    public String createNewStarvationZeroSpendTransaction(TransactionData transactionData) {
        return createNewZeroSpendTransaction(transactionData, STARVATION);
    }

    public String createNewGenesisZeroSpendTransaction(ZeroSpendTransactionRequest zeroSpendTransactionRequest) {
        return createNewZeroSpendTransaction(zeroSpendTransactionRequest.getTransactionData(), GENESIS);
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
        } else {
            transactionData.setGenesis(true);
        }

        DspConsensusResult dspConsensusResult = new DspConsensusResult(transactionData.getHash());
        dspConsensusResult.setDspConsensus(true);
        dspVoteService.setIndexForDspResult(transactionData, dspConsensusResult);
        transactionHelper.attachTransactionToCluster(transactionData);
        transactionIndexService.insertNewTransactionIndex(transactionData);
        log.info("Created a new Zero Spend Transaction: Hash = {} , SenderTrustScore = {} ", transactionData.getHash(), transactionData.getSenderTrustScore());
        return transactionData;
    }

    private void sendTransactionToPublisher(TransactionData transactionData) {
        log.info("Sending Zero Spend Transaction to DSPs. transaction: Hash = {} , SenderTrustScore = {}", transactionData.getHash(), transactionData.getSenderTrustScore());
        propagationPublisher.propagate(transactionData, Arrays.asList(NodeType.DspNode, NodeType.TrustScoreNode));

    }

    public void createGenesisTransactions() {
        log.info("Creating genesis transactions");
        for (int trustScore = 0; trustScore <= 100; trustScore = trustScore + 10) {
            TransactionData transactionData = createZeroSpendTransactionData(trustScore, GENESIS);

            DspConsensusResult dspConsensusResult = new DspConsensusResult(transactionData.getHash());
            dspConsensusResult.setDspConsensus(true);
            dspVoteService.setIndexForDspResult(transactionData, dspConsensusResult);
            transactionData.setGenesis(true);

            transactionHelper.attachTransactionToCluster(transactionData);
        }
    }

    private TransactionData createZeroSpendTransactionData(double trustScore, ZeroSpendTransactionType description) {
        List<BaseTransactionData> baseTransactions = new ArrayList<>();
        BaseTransactionData baseTransactionData = new InputBaseTransactionData(transactionCryptoCreator.getAddress(), BigDecimal.ZERO, "Transfer", new Date());
        baseTransactions.add(baseTransactionData);
        TransactionData transactionData = new TransactionData(baseTransactions, description.name(), trustScore, new Date());
        transactionData.setAttachmentTime(new Date());
        transactionData.setZeroSpend(true);

        transactionCryptoCreator.signBaseTransactions(transactionData);
        transactionCrypto.signMessage(transactionData);
        return transactionData;
    }
}