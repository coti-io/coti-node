package io.coti.zerospend.services;

import io.coti.common.communication.DspVote;
import io.coti.common.crypto.NodeCryptoHelper;
import io.coti.common.data.Hash;
import io.coti.common.data.NonIndexedTransactionsData;
import io.coti.common.data.TransactionData;
import io.coti.common.model.NonIndexedTransactions;
import io.coti.common.model.Transactions;
import io.coti.common.services.TransactionHelper;
import io.coti.common.services.interfaces.IValidationService;
import io.coti.zerospend.DspCsvImporter;
import io.coti.zerospend.services.interfaces.IDspPropagationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class DspPropagationService implements IDspPropagationService {

    @Autowired
    private IValidationService validationService;

    @Autowired
    private TransactionHelper transactionHelper;

    @Autowired
    private NonIndexedTransactions nonIndexedTransactions;

    @Autowired
    private Transactions transactions;

    @Autowired
    private NodeCryptoHelper nodeCryptoHelper;

    @Autowired
    private DspCsvImporter dspCsvImporter;

    private Map<String, Hash> hostNameToDspHash;

    private static final String GET_NODE_HASH_API_ACTION_NAME = "/getNodeHash"; // aka ping

    @PostConstruct
    private void init() {
        hostNameToDspHash = dspCsvImporter.getHost2NodeHashMap();
    }

    public void handlePropagatedTransactionFromDSP(TransactionData transactionData) {
        checkDspWithHttp();
        saveTransactionAndUpdateDAG(transactionData);
        log.info("Transaction {} was saved in transaction table", transactionData);
        NonIndexedTransactionsData nonIndexedTransactionsData = null;
        try {
            nonIndexedTransactionsData = nonIndexedTransactions.getByHash(transactionData.getHash());
        } catch (Exception ex) {
            log.info("Transaction wasn't found in db", ex);
        }
        try {
            if (!nodeCryptoHelper.verifyTransactionSignature(transactionData)) {
                log.error("Error on signing verification with dsp {}", transactionData.getNodeHash());
                // TODO: Throw exception ?
            }
        } catch (Exception ex) {
            log.error("Security Exception!", ex);
            // TODO: Throw exception ?
        }
        for (Hash dspHash : hostNameToDspHash.values()) {
            DspVote dspVote = new DspVote(transactionData.getHash(), dspHash, null, null);
            if (nonIndexedTransactionsData == null) {
                Map<Hash, DspVote> hashDspVoteHashMap = new HashMap<>();
                hashDspVoteHashMap.put(dspHash, dspVote);
                nonIndexedTransactionsData = new NonIndexedTransactionsData(transactionData.getHash(), hashDspVoteHashMap);
            } else {
                nonIndexedTransactionsData.getDspVoteMap().put(dspHash, dspVote);
            }
        }
        nonIndexedTransactions.put(nonIndexedTransactionsData);
        log.info("Non indexed trx {} was saved in nonIndexedTransactions table", nonIndexedTransactionsData);
    }


    private void saveTransactionAndUpdateDAG(TransactionData transactionData) {
        if (!transactionHelper.startHandleTransaction(transactionData)) {
            log.info("Transaction already exists");
        }

        if (!transactionHelper.validateTransaction(transactionData) ||
                !NodeCryptoHelper.verifyTransactionSignature(transactionData) ||
                !validationService.validatePow(transactionData) ||
                !transactionHelper.checkBalancesAndAddToPreBalance(transactionData)) {
            log.info("Invalid Transaction Received!");
        }
        transactions.put(transactionData);
        transactionHelper.setTransactionStateToSaved(transactionData);
        if (!transactionHelper.checkBalancesAndAddToPreBalance(transactionData)) {
            transactionData.addSignature("Node ID", false); // TODO: replace with a sign mechanism
        }
        transactionHelper.attachTransactionToCluster(transactionData);

        transactionHelper.setTransactionStateToFinished(transactionData);
        transactionHelper.endHandleTransaction(transactionData);
    }

    private void checkDspWithHttp() {
        RestTemplate restTemplate = new RestTemplate();
        for (Map.Entry<String, Hash> entry : hostNameToDspHash.entrySet()) {
            Hash voterNodeHash = restTemplate.getForObject(entry.getKey() + GET_NODE_HASH_API_ACTION_NAME, Hash.class);
            if (voterNodeHash == null) {
                log.info("Dsp is down {}", entry);
                // TODO : Throw exception ?
            } else {
                if (!entry.getValue().equals(voterNodeHash)) {
                    // TODO : Throw exception ?
                    log.error("Malicious DSP! {} ", entry);
                }
            }
        }
    }

    public Map<String, Hash> getHostNameToDspHash() {
        return hostNameToDspHash;
    }

}
