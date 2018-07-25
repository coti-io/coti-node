package io.coti.zerospend.services;

import io.coti.common.data.Hash;
import io.coti.common.data.TransactionData;
import io.coti.common.services.ZeroMQTransactionReceiver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Service
@Slf4j
public class DspVoteReceiver {

    private int numberOfDSP = 3;
    private int numberOfDSP = 4;

    private Map<Hash, List<Boolean>> transactionsVotes;

    @Autowired
    private ZeroMQTransactionReceiver zeroMQTransactionReceiver;

    @Autowired
    private DspVoteDecisionPublisher dspVoteDecisionPublisher;

    private Function<TransactionData, String> newTransactionFromFullNodeHandler = transactionData -> {

        if (transactionData != null) {
            insertVote(transactionData);
            return "GOOD!";
        } else {
            return "BAD!";
        }
    };

    @PostConstruct
    private void init() {
        transactionsVotes = new ConcurrentHashMap<>();
        try {
            zeroMQTransactionReceiver.init(newTransactionFromFullNodeHandler);
        } catch (Exception ex) {
            log.error("Exception in zeroMQTransactionReceiver init()", ex);
        }
    }


    private void insertVote(TransactionData transactionData) {
        Hash hash = transactionData.getHash();
        if (transactionsVotes.containsKey(hash)) {
            transactionsVotes.get(hash).add(transactionData.isVotePositive());
        } else {
            List<Boolean> singleTransactionVotes = new LinkedList<>();
            singleTransactionVotes.add(transactionData.isVotePositive());
            transactionsVotes.put(hash, singleTransactionVotes);
        }
        checkVotes(transactionData);

    }

    private void checkVotes(TransactionData transactionData) {
        Hash hash = transactionData.getHash();
        int numberOfDspVotes = transactionsVotes.get(hash).size();
        log.info("transaction {} now have {} votes", transactionData, numberOfDspVotes);
        double middle = numberOfDSP / 2;
        int majorityNumber = (int) Math.round(middle + 0.6);
        int numberOfPositiveVoters = 0;
        // go in
        if (majorityNumber <= numberOfDspVotes) { //

            log.info("Majority needed is {}", majorityNumber);
            for (boolean vote : transactionsVotes.get(hash)) {
                if (vote == true) {
                    numberOfPositiveVoters ++;
                }
            }
            if (numberOfPositiveVoters >= majorityNumber) {
                log.info("The transaction {} has more than {} positive votes", transactionData, majorityNumber);
                /*
                ############
                change the transaction validation field
                ############
                 */
            }
            else{
                log.info("The transaction {} didn't reach the threshold {} of positive votes"
                        , transactionData, majorityNumber);
                /*
                ############
                change the transaction validation field
                ############
                 */
            }
            dspVoteDecisionPublisher.publish(transactionData);
        }
    }


}
