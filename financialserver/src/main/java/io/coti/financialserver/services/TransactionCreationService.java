package io.coti.financialserver.services;

import io.coti.basenode.communication.interfaces.IPropagationPublisher;
import io.coti.basenode.crypto.DspConsensusCrypto;
import io.coti.basenode.crypto.TransactionCrypto;
import io.coti.basenode.data.*;
import io.coti.basenode.services.ClusterService;
import io.coti.basenode.services.TransactionHelper;
import io.coti.basenode.services.TransactionIndexService;
import io.coti.financialserver.crypto.TransactionCryptoCreator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class TransactionCreationService {

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
    private DspConsensusCrypto dspConsensusCrypto;
    @Autowired
    private ClusterService clusterService;
    @Autowired
    private RollingReserveService rollingReserveService;

    public void createNewChargebackTransaction(BigDecimal amount, Hash merchantRollingReserveAddress, Hash consumerAddress, BigDecimal poolAmount) {

        InputBaseTransactionData IBT = new InputBaseTransactionData(merchantRollingReserveAddress, amount.multiply(new BigDecimal(-1)), new Date());
        ReceiverBaseTransactionData RBT = new ReceiverBaseTransactionData(consumerAddress, amount, amount, new Date());

        List<BaseTransactionData> baseTransactions = new ArrayList<>();

        if (!poolAmount.equals(new BigDecimal(0))) {
            InputBaseTransactionData IBTcotiPool = new InputBaseTransactionData(rollingReserveService.getCotiPoolAddress(), poolAmount.multiply(new BigDecimal(-1)), new Date());
            baseTransactions.add(IBTcotiPool);
        }

        baseTransactions.add(IBT);
        baseTransactions.add(RBT);

        TransactionData chargebackTransaction = new TransactionData(baseTransactions);

        chargebackTransaction.setAttachmentTime(new Date());
        chargebackTransaction.setCreateTime(new Date());
        chargebackTransaction.setType(TransactionType.Chargeback);

        clusterService.selectSources(chargebackTransaction);

        transactionCryptoCreator.signBaseTransactions(chargebackTransaction);
        transactionCrypto.signMessage(chargebackTransaction);

        DspConsensusResult dspConsensusResult = new DspConsensusResult(chargebackTransaction.getHash());
        dspConsensusResult.setDspConsensus(true);

        setIndexForDspResult(chargebackTransaction, dspConsensusResult);
        transactionHelper.attachTransactionToCluster(chargebackTransaction);
        transactionIndexService.insertNewTransactionIndex(chargebackTransaction);

        propagationPublisher.propagate(chargebackTransaction, Arrays.asList(NodeType.DspNode, NodeType.TrustScoreNode));
    }

    private synchronized void setIndexForDspResult(TransactionData chargebackTransaction, DspConsensusResult dspConsensusResult) {

        dspConsensusResult.setIndex(transactionIndexService.getLastTransactionIndexData().getIndex() + 1);
        dspConsensusResult.setIndexingTime(new Date());

        dspConsensusCrypto.signMessage(dspConsensusResult);
        chargebackTransaction.setDspConsensusResult(dspConsensusResult);

        transactionIndexService.insertNewTransactionIndex(chargebackTransaction);
    }
}