package io.coti.zerospend.services;

import io.coti.common.communication.DspVote;
import io.coti.common.crypto.NodeCryptoHelper;
import io.coti.common.data.Hash;
import io.coti.common.data.NonIndexedTransactionsData;
import io.coti.common.data.TransactionData;
import io.coti.common.data.ZMQChannel;
import io.coti.common.model.NonIndexedTransactions;
import io.coti.common.model.Transactions;
import io.coti.zerospend.services.helper.TransactionHashWrapper;
import io.coti.zerospend.services.interfaces.IDspVoteService;
import io.coti.zerospend.services.interfaces.ITransactionIndexerService;
import io.coti.zerospend.services.interfaces.ITransactionPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class DspVoteService implements IDspVoteService {

    @Autowired
    private ITransactionPublisher dspVoteDecisionPublisher;

    @Autowired
    private NonIndexedTransactions nonIndexedTransactions;

    @Autowired
    private Transactions transactions;

    @Autowired
    private ITransactionIndexerService transactionIndexerService;

    @Autowired
    private NodeCryptoHelper nodeCryptoHelper;

    @Autowired
    private DspPropagationService dspPropagationService;

    private Map<Hash, TransactionHashWrapper> transactionHashWrapperMap;

    @PostConstruct
    private void init() {
        transactionHashWrapperMap = new ConcurrentHashMap<>();
    }

    public void insertVoteHelper(DspVote dspVote) {
        if (!dspPropagationService.getHostNameToDspHash().values().contains(dspVote.getVoterDspHash())) {
            // TODO: Throw exception ?
            log.error("This dsp is not allowed voting!! i.e not in the dsp list. bad dsp: {} list: {}"
                    , dspVote.getVoterDspHash(), dspPropagationService.getHostNameToDspHash().values());
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
        dspVoteDecisionPublisher.publish(transactionData, ZMQChannel.ZERO_SPEND_VOTING_ANSWER.name());
    }


}


