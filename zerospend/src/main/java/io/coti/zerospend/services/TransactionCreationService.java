package io.coti.zerospend.services;


import io.coti.basenode.communication.interfaces.IPropagationPublisher;
import io.coti.basenode.crypto.NodeCryptoHelper;
import io.coti.basenode.crypto.TransactionCrypto;
import io.coti.basenode.data.*;
import io.coti.basenode.http.Response;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.services.interfaces.*;
import io.coti.zerospend.crypto.TransactionCryptoCreator;
import io.coti.zerospend.data.ZeroSpendTransactionType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.STATUS_ERROR;
import static io.coti.zerospend.data.ZeroSpendTransactionType.GENESIS;
import static io.coti.zerospend.data.ZeroSpendTransactionType.STARVATION;

@Slf4j
@Service
public class TransactionCreationService {

    public static final int MAX_TRUST_SCORE = 100;
    private static final int ZERO_SPEND_ADDRESS_INDEX = 0;
    @Autowired
    private ITransactionHelper transactionHelper;
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
    private ICurrencyService currencyService;
    @Autowired
    private IEventService eventService;
    @Autowired
    private IClusterService clusterService;
    @Value("${zerospend.seed.key}")
    private String seed;

    public void createNewStarvationZeroSpendTransaction(TransactionData transactionData) {
        createNewZeroSpendTransaction(transactionData, STARVATION);
    }

    public void createNewGenesisZeroSpendTransaction(double trustScore) {
        TransactionData transactionData = createZeroSpendTransactionData(trustScore, GENESIS);

        attachAndSendZeroSpendTransaction(transactionData);
    }

    public void createNewZeroSpendTransaction(TransactionData existingTransactionData, ZeroSpendTransactionType zeroSpendTransactionType) {
        if (!validationService.fullValidation(existingTransactionData)) {
            log.error("Validation for waiting source  failed! requesting transaction {}", existingTransactionData);
            return;
        }
        log.debug("Creating a new Zero Spend Transaction for transaction : Hash = {} , SenderTrustScore = {}", existingTransactionData.getHash(), existingTransactionData.getSenderTrustScore());
        TransactionData transactionData = createZeroSpendTransactionData(existingTransactionData.getSenderTrustScore(), zeroSpendTransactionType);

        if (zeroSpendTransactionType == STARVATION) {
            transactionData.setLeftParentHash(existingTransactionData.getHash());
        }

        attachAndSendZeroSpendTransaction(transactionData);
    }


    private void attachAndSendZeroSpendTransaction(TransactionData transactionData) {
        attachTransactionToCluster(transactionData);
        sendTransactionToPublisher(transactionData);
    }

    private void attachTransactionToCluster(TransactionData transactionData) {
        DspConsensusResult dspConsensusResult = new DspConsensusResult(transactionData.getHash());
        dspConsensusResult.setDspConsensus(true);
        dspVoteService.setIndexForDspResult(transactionData, dspConsensusResult);
        transactionHelper.attachTransactionToCluster(transactionData);
        log.info("Created a new {} Transaction: Hash = {} , SenderTrustScore = {} ", transactionData.getTransactionDescription(), transactionData.getHash(), transactionData.getSenderTrustScore());
    }

    private void sendTransactionToPublisher(TransactionData transactionData) {
        log.debug("Sending {} Transaction. transaction: Hash = {} , SenderTrustScore = {}", transactionData.getTransactionDescription(), transactionData.getHash(), transactionData.getSenderTrustScore());
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
        Hash addressHash = NodeCryptoHelper.generateAddress(seed, ZERO_SPEND_ADDRESS_INDEX);
        BaseTransactionData baseTransactionData = new InputBaseTransactionData(addressHash, currencyService.getNativeCurrencyHash(), BigDecimal.ZERO, Instant.now());
        return createTransactionData(baseTransactionData, description.name(), trustScore, TransactionType.ZeroSpend, addressHash);
    }


    private TransactionData createEventTransactionData(String description, Event event) {
        Hash addressHash = NodeCryptoHelper.generateAddress(seed, ZERO_SPEND_ADDRESS_INDEX);
        EventInputBaseTransactionData eventInputBaseTransactionData = new EventInputBaseTransactionData(addressHash, currencyService.getNativeCurrencyHash(), BigDecimal.ZERO, Instant.now(),
                event);
        return createTransactionData(eventInputBaseTransactionData, description, MAX_TRUST_SCORE, TransactionType.EventHardFork, addressHash);
    }

    private TransactionData createTransactionData(BaseTransactionData baseTransactionData, String description, double trustScore, TransactionType transactionType,
                                                  Hash addressHash) {

        Map<Hash, Integer> addressHashToAddressIndexMap = new HashMap<>();
        List<BaseTransactionData> baseTransactions = new ArrayList<>();
        addressHashToAddressIndexMap.put(addressHash, ZERO_SPEND_ADDRESS_INDEX);
        baseTransactions.add(baseTransactionData);
        TransactionData transactionData = transactionHelper.createNewTransaction(baseTransactions, description, trustScore, Instant.now(), transactionType);
        transactionData.setAttachmentTime(Instant.now());
        transactionCryptoCreator.signBaseTransactions(transactionData, addressHashToAddressIndexMap);
        transactionCrypto.signMessage(transactionData);
        return transactionData;
    }

    public ResponseEntity<IResponse> createEventTransaction(String description, Event event) {
        TransactionData transactionData = createEventTransactionData(description, event);
        try {
            if (eventService.checkEventAndUpdateEventsTable(transactionData)) {
                clusterService.selectSources(transactionData);
                attachTransactionToCluster(transactionData);
                sendTransactionToPublisher(transactionData);
                return eventService.getEventTransactionDataResponse(event);
            } else {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST).body(new Response("Event Creation Error", STATUS_ERROR));
            }
        } catch (Exception e) {
            log.error(e.toString());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response(
                            e.toString(),
                            STATUS_ERROR));
        }
    }

}
