package io.coti.financialserver.services;

import io.coti.basenode.data.*;
import io.coti.basenode.exceptions.TransactionValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

import static io.coti.financialserver.services.NodeServiceManager.*;

@Slf4j
@Service
public class TransactionCreationService {

    public static final int MAX_TRUST_SCORE = 100;

    public void createNewChargebackTransaction(BigDecimal amount, Hash merchantRollingReserveAddress, Hash consumerAddress, BigDecimal poolAmount) {
        Hash nativeCurrencyHash = currencyService.getNativeCurrencyHash();
        InputBaseTransactionData ibt = new InputBaseTransactionData(merchantRollingReserveAddress, nativeCurrencyHash, amount.multiply(new BigDecimal(-1)), Instant.now());
        ReceiverBaseTransactionData rbt = new ReceiverBaseTransactionData(consumerAddress, nativeCurrencyHash, amount, nativeCurrencyHash, amount, Instant.now());

        List<BaseTransactionData> baseTransactions = new ArrayList<>();

        if (!poolAmount.equals(new BigDecimal(0))) {
            InputBaseTransactionData ibtCotiPool = new InputBaseTransactionData(rollingReserveService.getCotiRollingReserveAddress(), nativeCurrencyHash, poolAmount.multiply(new BigDecimal(-1)), Instant.now());
            baseTransactions.add(ibtCotiPool);
        }

        baseTransactions.add(ibt);
        baseTransactions.add(rbt);

        TransactionData chargebackTransaction = new TransactionData(baseTransactions);

        chargebackTransaction.setAttachmentTime(Instant.now());
        chargebackTransaction.setCreateTime(Instant.now());
        chargebackTransaction.setType(TransactionType.Chargeback);

        clusterService.selectSources(chargebackTransaction);

        //     transactionCryptoCreator.signBaseTransactions(chargebackTransaction)
        transactionCrypto.signMessage(chargebackTransaction);

        DspConsensusResult dspConsensusResult = new DspConsensusResult(chargebackTransaction.getHash());
        dspConsensusResult.setDspConsensus(true);

        nodeTransactionHelper.attachTransactionToCluster(chargebackTransaction);
        transactionIndexService.insertNewTransactionIndex(chargebackTransaction);

        propagationPublisher.propagate(chargebackTransaction, Arrays.asList(NodeType.DspNode, NodeType.TrustScoreNode));
    }

    public Hash createInitialTransaction(BigDecimal amount, Hash currencyHash, Hash cotiGenesisAddress, Hash fundAddress, int genesisAddressIndex) {
        if (currencyHash == null) {
            currencyHash = currencyService.getNativeCurrencyHash();
        }
        List<BaseTransactionData> baseTransactions = new ArrayList<>();

        InputBaseTransactionData ibt = new InputBaseTransactionData(cotiGenesisAddress, currencyHash, amount.multiply(new BigDecimal(-1)), Instant.now());

        ReceiverBaseTransactionData rbt = new ReceiverBaseTransactionData(fundAddress, currencyHash, amount, currencyHash, amount, Instant.now());
        baseTransactions.add(ibt);
        baseTransactions.add(rbt);

        TransactionData initialTransactionData = nodeTransactionHelper.createNewTransaction(baseTransactions, TransactionType.Initial.toString(), MAX_TRUST_SCORE, Instant.now(), TransactionType.Initial);

        if (!balanceService.checkBalancesAndAddToPreBalance(initialTransactionData.getBaseTransactions())) {
            throw new TransactionValidationException("Balance check failed");
        }
        clusterService.selectSources(initialTransactionData);
        initialTransactionData.setAttachmentTime(Instant.now());

        Map<Hash, Integer> addressHashToAddressIndexMap = new HashMap<>();
        addressHashToAddressIndexMap.put(cotiGenesisAddress, genesisAddressIndex);
        transactionCryptoCreator.signBaseTransactions(initialTransactionData, addressHashToAddressIndexMap);
        transactionCrypto.signMessage(initialTransactionData);
        nodeTransactionHelper.attachTransactionToCluster(initialTransactionData);

        propagationPublisher.propagate(initialTransactionData, Arrays.asList(NodeType.ZeroSpendServer, NodeType.TrustScoreNode, NodeType.FinancialServer, NodeType.DspNode, NodeType.HistoryNode));

        return initialTransactionData.getHash();
    }
}
