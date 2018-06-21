package io.coti.cotinode.service;

import io.coti.cotinode.data.Hash;

import io.coti.cotinode.data.TransactionData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Configurable
public class TccConfirmationService {
    private int TRESHOLD = 300;
    private Logger log = LoggerFactory.getLogger(this.getClass().getName());
    private ConcurrentHashMap<Hash, TransactionData> hashToUnTddConfirmTransactionsMapping;
    private LinkedList<TransactionData> result;

    public void init(ConcurrentHashMap<Hash, TransactionData> hashToUnConfirmationTransactionsMapping) {
        hashToUnTddConfirmTransactionsMapping = new ConcurrentHashMap<>();
        for (Map.Entry<Hash, TransactionData> entry : hashToUnConfirmationTransactionsMapping.entrySet()) {
            if (!entry.getValue().isTransactionConsensus()) {
                hashToUnTddConfirmTransactionsMapping.put(entry.getValue().getHash(), entry.getValue());
            }
        }
    }

    //takes adjacency list of a directed acyclic graph(DAG) as input
    //returns a linkedlist which consists the vertices in topological order
    public void topologicalSorting() {
        log.info("{} Starting topologicalSorting", Instant.now());
        result = new LinkedList<TransactionData>();

        // Reset
        for (Map.Entry<Hash, TransactionData> entry : hashToUnTddConfirmTransactionsMapping.entrySet()) {
           entry.getValue().setVisit(false);
        }

        //loop is for making sure that every vertex is visited since if we select only one random source
        //all vertices might not be reachable from this source
        //eg:1->2->3,1->3 and if we select 3 as source first then no vertex can be visited ofcourse except for 3
        for (Map.Entry<Hash, TransactionData> entry : hashToUnTddConfirmTransactionsMapping.entrySet()) {
            if (!entry.getValue().isVisit()) {
                topologicalSortingHelper(entry.getValue());
            }
        }
        synchronized(log) {
            log.info("{} Ending topologicalSorting()", Instant.now());
            result.forEach(transaction ->  log.info("{} After topologicalSorting in result: {}", Instant.now(), transaction.getHash()));

        }

    }

    private void setTotalSumScore(TransactionData parent) {
        int maxSonsTotalTrustScore = 0;
        Hash maxSonsTotalTrustScoreHash = null;
        for (Hash hash : parent.getChildrenTransactions()) {
            if (hashToUnTddConfirmTransactionsMapping.get(hash).getTotalTrustScore()
                    > maxSonsTotalTrustScore) {
                maxSonsTotalTrustScore = hashToUnTddConfirmTransactionsMapping.get(hash).getTotalTrustScore();
                maxSonsTotalTrustScoreHash = hash;
            }
        }

        // updating parent totalTrustScore
        parent.setTotalTrustScore(parent.getSenderTrustScore() + maxSonsTotalTrustScore);

        //updating parent trustChainTransactionHashes
        if (maxSonsTotalTrustScoreHash != null) { // not a source
            List<Hash> maxSonsTotalTrustScoreChain =
                    new Vector<>(hashToUnTddConfirmTransactionsMapping.get(maxSonsTotalTrustScoreHash).getTrustChainTransactionHashes());
            maxSonsTotalTrustScoreChain.add(maxSonsTotalTrustScoreHash);
            parent.setTrustChainTransactionHashes(maxSonsTotalTrustScoreChain);
        }
    }

    private void topologicalSortingHelper(TransactionData parentTransactionData) {
        for (Hash transactionDataHash : parentTransactionData.getChildrenTransactions()) {
            TransactionData childTransactionData = hashToUnTddConfirmTransactionsMapping.get(transactionDataHash);
            if (!childTransactionData.isVisit()) {
                topologicalSortingHelper(childTransactionData);
            }

        }
        //making the vertex visited for future reference
        parentTransactionData.setVisit(true);

        //pushing to the stack as departing
        result.addLast(parentTransactionData);
    }

    public List<Hash> setTransactionConsensus() {
        List<Hash> transactionConsensusConfirmed = new Vector<>();
        for(TransactionData transaction : result ) {
            setTotalSumScore(transaction);
            if (transaction.getTotalTrustScore() >= TRESHOLD) {
                transaction.setTransactionConsensus(true);
                transaction.setTransactionConsensusUpdateTime(new Date());
                transactionConsensusConfirmed.add(transaction.getHash());
            }
        }

        return transactionConsensusConfirmed;
    }

}
