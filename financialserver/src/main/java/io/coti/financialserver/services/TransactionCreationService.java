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
import java.util.*;

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

    public Hash createNewChargebackTransaction(BigDecimal amountFromRR, BigDecimal poolAmount, Hash merchantRollingReserveAddress, Map<Hash,BigDecimal> IBTAddressesWithAmountPercent) {

        BigDecimal totalChargebackAmount = amountFromRR.add(poolAmount);
        InputBaseTransactionData IBT = new InputBaseTransactionData(merchantRollingReserveAddress, amountFromRR.multiply(new BigDecimal(-1)), new Date());
        OutputBaseTransactionData OBT;

        List<BaseTransactionData> baseTransactions = new ArrayList<>();

        if (!poolAmount.equals(new BigDecimal(0))) {
            InputBaseTransactionData IBTCotiPool = new InputBaseTransactionData(rollingReserveService.getCotiPoolAddress(), poolAmount.multiply(new BigDecimal(-1)), new Date());
            baseTransactions.add(IBTCotiPool);
        }

        baseTransactions.add(IBT);

        for (Map.Entry<Hash, BigDecimal> IBTAddressWithAmountPercent : IBTAddressesWithAmountPercent.entrySet()) {
            OBT = new ChargebackBaseTransactionData(
                    IBTAddressWithAmountPercent.getKey(),
                    totalChargebackAmount.multiply(IBTAddressWithAmountPercent.getValue()),
                    totalChargebackAmount, new Date());
            baseTransactions.add(OBT);
        }

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
        return chargebackTransaction.getHash();
    }

    private synchronized void setIndexForDspResult(TransactionData chargebackTransaction, DspConsensusResult dspConsensusResult) {

        dspConsensusResult.setIndex(transactionIndexService.getLastTransactionIndexData().getIndex() + 1);
        dspConsensusResult.setIndexingTime(new Date());

        dspConsensusCrypto.signMessage(dspConsensusResult);
        chargebackTransaction.setDspConsensusResult(dspConsensusResult);

        transactionIndexService.insertNewTransactionIndex(chargebackTransaction);
    }
}