package io.coti.financialserver.services;

import io.coti.basenode.communication.interfaces.IPropagationPublisher;
import io.coti.basenode.crypto.TransactionCrypto;
import io.coti.basenode.data.*;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.ClusterService;
import io.coti.basenode.services.TransactionHelper;
import io.coti.basenode.services.TransactionIndexService;
import io.coti.financialserver.crypto.TransactionCryptoCreator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class TransactionCreationService {

    public static final int MAX_TRUST_SCORE = 100;
    @Autowired
    private TransactionIndexService transactionIndexService;
    @Autowired
    private TransactionHelper transactionHelper;
    @Autowired
    private IPropagationPublisher propagationPublisher;
    @Autowired
    private TransactionCrypto transactionCrypto;
    @Autowired
    private TransactionCryptoCreator transactionCryptoCreator;
    @Autowired
    private ClusterService clusterService;
    @Autowired
    private RollingReserveService rollingReserveService;
    @Autowired
    private Transactions transactions;

    public void createNewChargebackTransaction(BigDecimal amount, Hash merchantRollingReserveAddress, Hash consumerAddress, BigDecimal poolAmount) {

        InputBaseTransactionData ibt = new InputBaseTransactionData(merchantRollingReserveAddress, amount.multiply(new BigDecimal(-1)), Instant.now());
        ReceiverBaseTransactionData rbt = new ReceiverBaseTransactionData(consumerAddress, amount, amount, Instant.now());

        List<BaseTransactionData> baseTransactions = new ArrayList<>();

        if (!poolAmount.equals(new BigDecimal(0))) {
            InputBaseTransactionData ibtCotiPool = new InputBaseTransactionData(rollingReserveService.getCotiRollingReserveAddress(), poolAmount.multiply(new BigDecimal(-1)), Instant.now());
            baseTransactions.add(ibtCotiPool);
        }

        baseTransactions.add(ibt);
        baseTransactions.add(rbt);

        TransactionData chargebackTransaction = new TransactionData(baseTransactions);

        chargebackTransaction.setAttachmentTime(Instant.now());
        chargebackTransaction.setCreateTime(Instant.now());
        chargebackTransaction.setType(TransactionType.Chargeback);

        clusterService.selectSources(chargebackTransaction);

        transactionCryptoCreator.signBaseTransactions(chargebackTransaction);
        transactionCrypto.signMessage(chargebackTransaction);

        DspConsensusResult dspConsensusResult = new DspConsensusResult(chargebackTransaction.getHash());
        dspConsensusResult.setDspConsensus(true);

        transactionHelper.attachTransactionToCluster(chargebackTransaction);
        transactionIndexService.insertNewTransactionIndex(chargebackTransaction);

        propagationPublisher.propagate(chargebackTransaction, Arrays.asList(NodeType.DspNode, NodeType.TrustScoreNode));
    }

    public void createInitialTransactionToFund(BigDecimal amount, Hash cotiGenesisAddress, Hash fundAddress) {

        List<BaseTransactionData> baseTransactions = new ArrayList<>();

        InputBaseTransactionData ibt = new InputBaseTransactionData(cotiGenesisAddress, amount.multiply(new BigDecimal(-1)), Instant.now());
        ReceiverBaseTransactionData rbt = new ReceiverBaseTransactionData(fundAddress, amount, amount, Instant.now());
        baseTransactions.add(ibt);
        baseTransactions.add(rbt);

        double trustScore = MAX_TRUST_SCORE;
        TransactionData initialTransactionData = new TransactionData(baseTransactions, TransactionType.Initial.toString(), trustScore, Instant.now(), TransactionType.Initial);

        initialTransactionData.setAttachmentTime(Instant.now());

        transactionCryptoCreator.signBaseTransactions(initialTransactionData);
        transactionCrypto.signMessage(initialTransactionData);
        transactionHelper.attachTransactionToCluster(initialTransactionData);

        initialTransactionData = transactions.getByHash(initialTransactionData.getHash());
        propagationPublisher.propagate(initialTransactionData, Arrays.asList(NodeType.ZeroSpendServer));
    }
}