package io.coti.common.services;

import io.coti.common.data.Hash;
import io.coti.common.data.TransactionData;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Configurable
public class TccConfirmationService {
    @Value("${cluster.trust.chain.threshold}")
    private int threshold;
    private Logger log = LoggerFactory.getLogger(this.getClass().getName());
    private ConcurrentHashMap<Hash, TransactionData> hashToUnTccConfirmTransactionsMapping;
    private LinkedList<TransactionData> topologicalOrderedGraph;

    public void init(ConcurrentHashMap<Hash, TransactionData> hashToUnConfirmationTransactionsMapping) {
        hashToUnTccConfirmTransactionsMapping = new ConcurrentHashMap<>();
        for (Map.Entry<Hash, TransactionData> entry : hashToUnConfirmationTransactionsMapping.entrySet()) {
            if (!entry.getValue().isTrustChainConsensus()) {
                hashToUnTccConfirmTransactionsMapping.put(entry.getValue().getHash(), entry.getValue());
            }
        }
        topologicalOrderedGraph = new LinkedList<>();
        sortByTopologicalOrder();
    }

    private void sortByTopologicalOrder() {
        for (Map.Entry<Hash, TransactionData> entry : hashToUnTccConfirmTransactionsMapping.entrySet()) {
            entry.getValue().setVisit(false);
        }
        log.info(" sortByTopologicalOrder 2");
        //loop is for making sure that every vertex is visited since if we select only one random source
        //all vertices might not be reachable from this source
        //eg:1->2->3,1->3 and if we select 3 as source first then no vertex can be visited of course except for 3
        for (Map.Entry<Hash, TransactionData> entry : hashToUnTccConfirmTransactionsMapping.entrySet()) {
            log.info("iteration");
            if (!entry.getValue().isVisit()) {
                topologicalSortingHelper(entry.getValue());
            }
        }
    }

    private void setTotalTrustScore(TransactionData parent) {
        double maxSonsTotalTrustScore = 0;
        Hash maxSonsTotalTrustScoreHash = null;
        for (Hash hash : parent.getChildrenTransactions()) {
            try {
                if (hashToUnTccConfirmTransactionsMapping.get(hash).getTrustChainTrustScore()
                        > maxSonsTotalTrustScore) {
                    maxSonsTotalTrustScore = hashToUnTccConfirmTransactionsMapping.get(hash).getTrustChainTrustScore();
                    maxSonsTotalTrustScoreHash = hash;
                }
            } catch (Exception e) {
                log.error("in setTotalSumScore: parent: {} child: {}", parent.getHash(), hash );
                throw e;
            }
        }

        // updating parent trustChainTrustScore
        parent.setTrustChainTrustScore(parent.getSenderTrustScore() + maxSonsTotalTrustScore);

        //updating parent trustChainTransactionHashes
        if (maxSonsTotalTrustScoreHash != null) { // not a source
            List<Hash> maxSonsTotalTrustScoreChain =
                    new Vector<>(hashToUnTccConfirmTransactionsMapping.get(maxSonsTotalTrustScoreHash).getTrustChainTransactionHash());
            maxSonsTotalTrustScoreChain.add(maxSonsTotalTrustScoreHash);
            parent.setTrustChainTransactionHash(maxSonsTotalTrustScoreChain);
        }
    }

    private void topologicalSortingHelper(TransactionData parentTransactionData) {
        //log.info("starting topologicalSortingHelper");
        for (Hash transactionDataHash : parentTransactionData.getChildrenTransactions()) {
            //log.info("topologicalSortingHelper iteration");
            TransactionData childTransactionData = hashToUnTccConfirmTransactionsMapping.get(transactionDataHash);
            if (!childTransactionData.isVisit()) {
                topologicalSortingHelper(childTransactionData);
            }

        }
        //making the vertex visited for future reference
        parentTransactionData.setVisit(true);

        //pushing to the stack as departing
        topologicalOrderedGraph.addLast(parentTransactionData);
    }

    public List<Hash> getTccConfirmedTransactions() {
        List<Hash> transactionConsensusConfirmed = new LinkedList<>();
        for(TransactionData transaction : topologicalOrderedGraph) {
            setTotalTrustScore(transaction);
            if (transaction.getTrustChainTrustScore() >= threshold) {
                transaction.setTrustChainConsensus(true);
                transaction.setTransactionConsensusUpdateTime(new Date());
                transactionConsensusConfirmed.add(transaction.getHash());
                log.info("transaction with hash:{} is confirmed with trustScore: {} and totalTrustScore:{} ", transaction.getHash(),transaction.getSenderTrustScore(),  transaction.getTrustChainTrustScore());
                log.info("Trust Chain Transaction Hashes of transaction {}", Arrays.toString(transaction.getTrustChainTransactionHash().toArray()));
                for(Hash hash: transaction.getTrustChainTransactionHash()) {
                    log.info(hash.toString());
                }
                log.info("end of trust chain");
            }
        }

        return transactionConsensusConfirmed;
    }

}
