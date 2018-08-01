package io.coti.zerospend.services;

import io.coti.common.communication.DspVote;
import io.coti.common.communication.interfaces.IPropagationSubscriber;
import io.coti.common.communication.interfaces.IReceiver;
import io.coti.common.crypto.NodeCryptoHelper;
import io.coti.common.data.Hash;
import io.coti.common.data.NonIndexedTransactionsData;
import io.coti.common.data.SignatureData;
import io.coti.common.data.TransactionData;
import io.coti.common.model.NonIndexedTransactions;
import io.coti.common.model.Transactions;
import io.coti.zerospend.DspCsvImporter;
import io.coti.zerospend.services.helper.TransactionHashWrapper;
import io.coti.zerospend.services.interfaces.IDspVoteDecisionPublisher;
import io.coti.zerospend.services.interfaces.ITransactionIndexerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

@Component
@Slf4j
public class DspVoteReceiver {
    @Autowired
    private IReceiver zeroMQTransactionReceiver;

    @Autowired
    private IDspVoteDecisionPublisher dspVoteDecisionPublisher;

    @Autowired
    private NonIndexedTransactions nonIndexedTransactions;

    @Autowired
    private Transactions transactions;

    @Autowired
    private ITransactionIndexerService transactionIndexerService;

    @Autowired
    private IPropagationSubscriber propagationSubscriber;

    @Autowired
    private NodeCryptoHelper nodeCryptoHelper;

    @Autowired
    private DspCsvImporter dspCsvImporter;

    private Map<String, Hash> hostNameToDspHash;

    private final String dspGetNodeHashAction = "/getNodeHash"; // aka ping

    private Map<Hash, TransactionHashWrapper> transactionHashWrapperMap;

    private Function<Object, String> newDspVoteFromDsp = dspVote -> {
        if (dspVote != null) {
            if (dspVote instanceof DspVote) {
                insertVoteHelper((DspVote) dspVote);
                return "GOOD!";
            } else {
                return "BAD!";
            }
        } else {
            return "BAD!";
        }
    };

    private Consumer<Object> newTransactionFromDsp = transactionData -> {
        if (transactionData != null) {
            if (transactionData instanceof TransactionData) {
                acceptPropagation((TransactionData) transactionData);
            } else {
                // TODO: Throw exception?
            }
        } else {
            // TODO: Throw exception?
        }
    };


    @PostConstruct
    private void init() {
        Map<String, Consumer<Object>> messageHandler = new HashMap<>();
        messageHandler.put(TransactionData.class.getName() + "DSP Nodes", newTransactionFromDsp);
        propagationSubscriber.init(messageHandler);
        hostNameToDspHash = dspCsvImporter.getHost2NodeHashMap();

        Map<String, Function<Object, String>> voteMapping = new HashMap<>();
        voteMapping.put(DspVote.class.getName(), newDspVoteFromDsp);
        zeroMQTransactionReceiver.init(voteMapping);
        transactionHashWrapperMap = new ConcurrentHashMap<>();

    }

    // @Scheduled
    private void checkDspWithHttp() {
        RestTemplate restTemplate = new RestTemplate();
        for (Map.Entry<String, Hash> entry : hostNameToDspHash.entrySet()) {
            Hash voterNodeHash = restTemplate.getForObject(entry.getKey() + dspGetNodeHashAction, Hash.class);
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

    private void acceptPropagation(TransactionData transactionData) {
        saveTransaction(transactionData);
    }

    private void saveTransaction(TransactionData transactionData) {
        checkDspWithHttp();
        transactions.put(transactionData);
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

    public void addTransactionPublic(TransactionData transactionData) {
        saveTransaction(transactionData);
    }

    public void insertVotePublic(DspVote dspVote) {
        insertVoteHelper(dspVote);
    }

    private void insertVoteHelper(DspVote dspVote) {
        if(!hostNameToDspHash.values().contains(dspVote.getVoterDspHash())){
            // TODO: Throw exception ?
            log.error("This dsp is not allowed voting!! i.e not in the dsp list. bad dsp: {} list: {}"
                    , dspVote.getVoterDspHash(), hostNameToDspHash.values());
        }
        try {
            if (!nodeCryptoHelper.verifyVoteSignature(dspVote)) {
                log.error("Error on signing verification with dsp {}", dspVote.getVoterDspHash());
                // TODO: Throw exception ?
            }
        } catch (Exception ex) {
            log.error("Security Exception!", ex);
        }
        TransactionHashWrapper transactionHashWrapper;
        if (!transactionHashWrapperMap.containsKey(dspVote.getTransactionHash())) {
            transactionHashWrapper = new TransactionHashWrapper(dspVote.getTransactionHash());
            transactionHashWrapperMap.put(transactionHashWrapper.getHash(), transactionHashWrapper);
        } else {
            transactionHashWrapper = transactionHashWrapperMap.get(dspVote.getTransactionHash());
        }

        transactionHashWrapper.checkSync(); // checkUsage()?
        try {
            insertVote(dspVote);
        } catch (Exception ex) {
            log.error("Error in insertVote", ex);
            transactionHashWrapper.setVoting(false);
            notifyHash(transactionHashWrapper);
            return;
        }
        transactionHashWrapper.setVoting(false);
        notifyHash(transactionHashWrapper);
        log.info("Dsp {} voting finished", dspVote.getVoterDspHash());
    }


    private void insertVote(DspVote dspVote) {
        Hash transactionHash = dspVote.getTransactionHash();
        NonIndexedTransactionsData nonIndexedTransactionsData = null;
        try {
            nonIndexedTransactionsData = nonIndexedTransactions
                    .getByHash(transactionHash);
        } catch (Exception ex) {
            log.info("Transaction {} wasn't found in the db", transactionHash);
        }
        if (nonIndexedTransactionsData == null) {
            log.error("NonIndexedTransaction {} was not found in the db", transactionHash);
            //TODO: throw exception  ??
        }
        Map<Hash, DspVote> dspS = nonIndexedTransactionsData.getDspVoteMap();
        DspVote dspVoteFromDB = dspS.get(dspVote.getVoterDspHash());
        if (dspVoteFromDB == null) {
            log.error("This message didn't come from the right dsp node! wrong node: {} existing nodeList: {}"
                    , dspVote.getVoterDspHash(), dspS);
            //TODO: throw exception  ??
        } else {
            if (dspVoteFromDB.getIsValidTransaction() != null) {
                log.error("This DSP already voted!!!");
                // TODO: throw exception  ??
            } else {
                dspVoteFromDB.setIsValidTransaction(dspVote.getIsValidTransaction());
                dspVoteFromDB.setSignature(dspVote.getSignature());
                nonIndexedTransactions.put(nonIndexedTransactionsData);
            }
            int numberOfPositiveVotes = 0;
            int numberOfNegativeVotes = 0;
            List<Boolean> votes = new LinkedList<>();
            for (Map.Entry<Hash, DspVote> dsp : dspS.entrySet()) {
                Boolean vote = dsp.getValue().getIsValidTransaction();
                if (vote != null) {
                    votes.add(vote);
                    if (vote.booleanValue() == true) {
                        numberOfPositiveVotes++;
                    } else {
                        numberOfNegativeVotes++;
                    }
                }
            }
            if (votes.size() == dspS.size()) {
                log.info("All of the dspS has voted: {}", dspS);
                TransactionData transactionData = transactions.getByHash(transactionHash);
                checkVotesIndexAndPublish(numberOfPositiveVotes, numberOfNegativeVotes, transactionData);
            }
        }
    }

    private void notifyHash(TransactionHashWrapper wrapper) {
        synchronized (wrapper) {
            wrapper.notify();
        }
    }

    private void checkVotesIndexAndPublish(int numberOfPositiveVotes, int numberOfNegativeVotes, TransactionData transactionData) {
        if (numberOfPositiveVotes > numberOfNegativeVotes) {
            log.info("Transaction Approved by zero spend! Transaction: {} positive votes: {}. negative votes: {}.",
                    transactionData.getHash(), numberOfPositiveVotes, numberOfNegativeVotes);
            transactionData.setValidationApprovedByZeroSpend(true);
        } else if (numberOfNegativeVotes > numberOfPositiveVotes) {
            log.info("Transaction declined by zero spend! Transaction: {} positive votes: {}. negative votes: {}.",
                    transactionData.getHash(), numberOfPositiveVotes, numberOfNegativeVotes);
            transactionData.setValidationApprovedByZeroSpend(false);
        } else {
            // TODO: What todo on a tie
        }
        transactionHashWrapperMap.remove(transactionData.getHash());
        // TODO : nonIndexedTransactions.delete(transactionData.getHash()); ???
        transactionIndexerService.generateAndSetTransactionIndex(transactionData);
        dspVoteDecisionPublisher.publish(transactionData);
    }


}


