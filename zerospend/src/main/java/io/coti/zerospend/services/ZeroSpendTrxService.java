package io.coti.zerospend.services;

import io.coti.common.communication.interfaces.IReceiver;
import io.coti.common.crypto.NodeCryptoHelper;
import io.coti.common.data.BaseTransactionData;
import io.coti.common.data.Hash;
import io.coti.common.data.SignatureData;
import io.coti.common.data.TransactionData;
import io.coti.common.services.TransactionHelper;
import io.coti.common.services.interfaces.IBalanceService;
import io.coti.common.services.interfaces.IValidationService;
import io.coti.zerospend.monitor.interfaces.IAccessMonitor;
import io.coti.zerospend.services.interfaces.ITransactionIndexerService;
import io.coti.zerospend.services.interfaces.ITransactionPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;

@Slf4j
@Service
public class ZeroSpendTrxService {

    private static final Hash ZERO_SPEND_BASE_TRX_ADDRESS_HASH = new Hash("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF\" +\n" +
            "               \"FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF");
    private static final Hash ZERO_SPEND_BASE_TRX_HASH = new Hash("BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB" +
            "BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB");
    private static final Hash ZERO_SPEND_TRANSACTION_HASH = new Hash("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" +
            "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
    private static final String ZERO_SPEND_NO_SOURCE_CHANNEL = "ZeroSpend No sources";
    private static final String ZERO_SPEND_STARVATION_CHANNEL = "ZeroSpend Source Validation";

    private final SignatureData signatureData = new SignatureData("", "");
    private final String zeroSpendDescription = "zero spend";
    private final BigDecimal zeroSpendAmount = new BigDecimal(0);
    @Autowired
    private IAccessMonitor monitorAccess;

    @Autowired
    private ITransactionIndexerService transactionIndexerService;

    @Autowired
    private IReceiver zeroMQTransactionReceiver;

    @Autowired
    private TransactionHelper transactionHelper;

    @Autowired
    private IBalanceService balanceService;

    @Autowired
    private ITransactionPublisher transactionPublisher;

    @Autowired
    private IValidationService validationService;

    @Autowired
    private NodeCryptoHelper nodeCryptoHelper;

    private Function<Object, String> newZsTransactionRequest = transactionData -> {
        if (transactionData != null) {
            if (transactionData instanceof TransactionData) {
                receiveZeroSpendTransaction((TransactionData) transactionData);
                return "GOOD!";
            } else {
                return "BAD!";
            }
        } else {
            return "BAD!";
        }
    };

    @PostConstruct
    private void init() {
        HashMap<String, Function<Object, String>> voteMapping = new HashMap<>();
        voteMapping.put(TransactionData.class.getName() + "ZeroSpendNotEnoughSources", newZsTransactionRequest);
        zeroMQTransactionReceiver.init(voteMapping);
    }


    public void publicReceiveZeroSpendTransaction(TransactionData transactionData){
        receiveZeroSpendTransaction(transactionData);
    }

    private void receiveZeroSpendTransaction(TransactionData transactionData) {
        if (!monitorAccess.insertAndValidateAccessEvent(transactionData.getNodeHash())) {
            log.error("The node {} has reached the limit of requests ", transactionData.getNodeHash());
            TransactionData badTransactionData = new TransactionData(new LinkedList<>(), ZERO_SPEND_TRANSACTION_HASH,
                    "The node reached the limit of requests ", -1, new Date());
        }

        if (transactionHelper.isTransactionExists(transactionData)) {
            log.error("Transaction {} already exists", transactionData);
            return;
        }

        if (//!transactionHelper.validateDataIntegrity(transactionData) ||
                !NodeCryptoHelper.verifyTransactionSignature(transactionData) ||
                !validationService.validatePow(transactionData) ||
                !balanceService.checkBalancesAndAddToPreBalance(transactionData.getBaseTransactions()) ||
                !validTrustScore()) {
            log.info("Invalid Transaction ( {} )Received!", transactionData.getHash());
        }

        if (transactionData.isSource()) {
            handleZeroSpendSourceStarvation(transactionData);
        } else {
            handleZeroSpendNoSources(transactionData);
        }
    }

    private void handleZeroSpendNoSources(TransactionData transactionData) {
        TransactionData zsTransactionData = createZsTransaction(transactionData);
        transactionPublisher.publish(zsTransactionData, ZERO_SPEND_NO_SOURCE_CHANNEL);
    }

    private void handleZeroSpendSourceStarvation(TransactionData transactionData) {
        TransactionData zsTransactionData = createZsTransaction(transactionData);
        transactionPublisher.publish(zsTransactionData, ZERO_SPEND_STARVATION_CHANNEL);
    }

    private TransactionData createZsTransaction(TransactionData transactionData) {
        BaseTransactionData baseTransactionData = new BaseTransactionData(ZERO_SPEND_BASE_TRX_ADDRESS_HASH,
                zeroSpendAmount, ZERO_SPEND_BASE_TRX_HASH, signatureData, new Date());
        List<BaseTransactionData> baseTransactionDataList = new LinkedList<>();
        baseTransactionDataList.add(baseTransactionData);
        TransactionData zsTransactionData = new TransactionData(baseTransactionDataList, ZERO_SPEND_TRANSACTION_HASH,
                zeroSpendDescription, transactionData.getSenderTrustScore(), new Date());
        addSignature(zsTransactionData);// TODO: YOHAI SIGNING FEATURE
        zsTransactionData.setNodeHash(new Hash(NodeCryptoHelper.getNodePublicKey()));
        transactionIndexerService.generateAndSetTransactionIndex(transactionData);
        return zsTransactionData;
    }


    private boolean validTrustScore() {
        return true;
    }

    private void addSignature(TransactionData transactionData) {
        // TODO: YOHAI SIGNING FEATURE
    }


}
