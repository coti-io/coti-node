package io.coti.zerospend.services;

import io.coti.common.communication.interfaces.IPropagationPublisher;
import io.coti.common.crypto.*;
import io.coti.common.data.BaseTransactionData;
import io.coti.common.data.Hash;
import io.coti.common.data.TransactionData;
import io.coti.common.data.ZeroSpendDescription;
import io.coti.common.services.TransactionHelper;
import io.coti.common.services.interfaces.IValidationService;
import io.coti.zerospend.monitor.interfaces.IAccessMonitor;
import io.coti.zerospend.services.interfaces.ITransactionIndexService;
import io.coti.zerospend.services.interfaces.IZeroSpendTrxService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

@Slf4j
@Service
public class ZeroSpendTransactionService implements IZeroSpendTrxService {
    private final BigDecimal zeroSpendAmount = new BigDecimal(0);
    @Value("${global.private.key}")
    private String zeroSpendGlobalPrivateKey;
    @Autowired
    private IAccessMonitor monitorAccess;

    @Autowired
    private ITransactionIndexService transactionIndexerService;

    @Autowired
    private TransactionHelper transactionHelper;

    @Autowired
    private IValidationService validationService;

    @Autowired
    private IPropagationPublisher transactionPropagationPublisher;

    @Autowired
    private TransactionCrypto transactionCrypto;

    public void receiveZeroSpendTransaction(TransactionData incomingTransactionData) {
        if (incomingTransactionData.isSource() && incomingTransactionData.getAttachmentTime() != null) {
            handleZeroSpendSourceStarvation(incomingTransactionData);
        } else {
            handleZeroSpendNoSources(incomingTransactionData);
        }
    }

    private void handleZeroSpendNoSources(TransactionData transactionDataFromDSP) {
        validateMonitoring(transactionDataFromDSP);
        String channelName = ZeroSpendDescription.ZERO_SPEND_TRANSACTION_NO_SOURCE.name();
        if (!validateNoSourceZS(transactionDataFromDSP)) {
            log.error("validation for noSourceZS failed! requesting transaction {}", transactionDataFromDSP);
        }

        TransactionData zsTransactionData = createZsTransaction(transactionDataFromDSP, channelName);
        sendTransactionToPublisher(zsTransactionData, channelName);
    }


    private void handleZeroSpendSourceStarvation(TransactionData source) {
        String channelName = ZeroSpendDescription.ZERO_SPEND_TRANSACTION_STARVATION.name();
        if (!validateStarvingSourcesZS(source)) {
            log.error("Validation for starving source zs failed! requesting transaction {}", source);
        }
        TransactionData zsTransactionData = createZsTransaction(source,
                channelName);
        sendTransactionToPublisher(zsTransactionData,
                channelName);
    }

    private TransactionData createZsTransaction(TransactionData transactionData, String description) {
        BaseTransactionData baseTransactionData = new BaseTransactionWithPrivateKey(zeroSpendAmount, new Date(),
                zeroSpendGlobalPrivateKey);
        List<BaseTransactionData> baseTransactionDataList = new LinkedList<>();
        baseTransactionDataList.add(baseTransactionData);
        TransactionData zsTransactionData = new TransactionData(baseTransactionDataList, null,
                description, transactionData.getSenderTrustScore(), new Date());
        addSignature(zsTransactionData);
        zsTransactionData.setNodeHash(NodeCryptoHelper.getNodeHash());
        transactionIndexerService.generateAndSetTransactionIndex(zsTransactionData);
        zsTransactionData.setAttachmentTime(new Date());
        transactionCrypto.signMessage(zsTransactionData);
        transactionHelper.attachTransactionToCluster(zsTransactionData);
        return zsTransactionData;
    }

    private boolean validTrustScore() {
        return true;
    }

    private boolean validateStarvingSourcesZS(TransactionData source) {
        return validationService.fullValidation(source);
    }

    private boolean validateNoSourceZS(TransactionData transactionDataFromDSP) {
        if (transactionHelper.isTransactionExists(transactionDataFromDSP)) {
            log.error("Transaction {} already exists", transactionDataFromDSP);
            return false;
        }

        if (!transactionHelper.validateTransaction(transactionDataFromDSP) ||
                !validationService.validatePow(transactionDataFromDSP) ||
                !validTrustScore()) {
            log.info("Invalid Transaction ( {} )Received!", transactionDataFromDSP.getHash());
            return false;
        }
        return true;
    }

    private void validateMonitoring(TransactionData transactionDataFromDSP) {
        if (!monitorAccess.insertAndValidateAccessEvent(transactionDataFromDSP.getNodeHash())) {
            log.error("The node {} has reached the limit of requests ", transactionDataFromDSP.getNodeHash());
            TransactionData badTransactionData = new TransactionData(new LinkedList<>(), null,
                    "The node reached the limit of requests ", -1, new Date());
            sendTransactionToPublisher(badTransactionData,
                    ZeroSpendDescription.ZERO_SPEND_TRANSACTION_NO_SOURCE.name());
        }

    }

    private void addSignature(TransactionData transactionData) {
        TransactionCyptoCreator txCreator = new TransactionCyptoCreator(transactionData);
        txCreator.signTransaction();
    }

    private void sendTransactionToPublisher(TransactionData transactionData, String channelName) {
        log.info("About to send zs transaction to DSPs. transaction: {}  channel: {}", transactionData, channelName);
        transactionPropagationPublisher.propagate(transactionData, TransactionData.class.getName() + channelName);
    }


}
