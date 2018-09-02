package io.coti.basenode.services;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TccInfo;
import io.coti.basenode.data.TransactionData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@Configurable
public class TccConfirmationService {
    @Value("${cluster.trust.chain.threshold}")
    private int threshold;
    private ConcurrentHashMap<Hash, TransactionData> hashToTccUnConfirmTransactionsMapping;
    private LinkedList<TransactionData> topologicalOrderedGraph;

    public void init(ConcurrentHashMap<Hash, TransactionData> hashToUnConfirmationTransactionsMapping) {
        hashToTccUnConfirmTransactionsMapping = new ConcurrentHashMap<>(hashToUnConfirmationTransactionsMapping);
        topologicalOrderedGraph = new LinkedList<>();
        sortByTopologicalOrder();
    }

    private void sortByTopologicalOrder() {
        hashToTccUnConfirmTransactionsMapping.forEach((hash, transactionData) -> transactionData.setVisit(false));

        //loop is for making sure that every vertex is visited since if we select only one random source
        //all vertices might not be reachable from this source
        //eg:1->2->3,1->3 and if we select 3 as source first then no vertex can be visited of course except for 3
        hashToTccUnConfirmTransactionsMapping.forEach((hash, transactionData) -> {
            if (!transactionData.isVisit()) {
                topologicalSortingHelper(transactionData);
            }
        });
    }

    private void setTotalTrustScore(TransactionData parent) {
        double maxSonsTotalTrustScore = 0;
        Hash maxSonsTotalTrustScoreHash = null;
        for (Hash hash : parent.getChildrenTransactions()) {
            try {
                TransactionData child = hashToTccUnConfirmTransactionsMapping.get(hash);
                if (child != null && child.getTrustChainTrustScore()
                        > maxSonsTotalTrustScore) {
                    maxSonsTotalTrustScore = hashToTccUnConfirmTransactionsMapping.get(hash).getTrustChainTrustScore();
                    maxSonsTotalTrustScoreHash = hash;
                }
            } catch (Exception e) {
                log.error("in setTotalSumScore: parent: {} child: {}", parent.getHash(), hash);
                throw e;
            }
        }

        // updating parent trustChainTrustScore
        parent.setTrustChainTrustScore(parent.getSenderTrustScore() + maxSonsTotalTrustScore);

        //updating parent trustChainTransactionHashes
        if (maxSonsTotalTrustScoreHash != null) { // not a source
            List<Hash> maxSonsTotalTrustScoreChain =
                    new Vector<>(hashToTccUnConfirmTransactionsMapping.get(maxSonsTotalTrustScoreHash).getTrustChainTransactionHashes());
            maxSonsTotalTrustScoreChain.add(maxSonsTotalTrustScoreHash);
            parent.setTrustChainTransactionHashes(maxSonsTotalTrustScoreChain);
        }
    }

    private void topologicalSortingHelper(TransactionData parentTransactionData) {
        for (Hash transactionDataHash : parentTransactionData.getChildrenTransactions()) {
            TransactionData childTransactionData = hashToTccUnConfirmTransactionsMapping.get(transactionDataHash);
            if (childTransactionData != null && !childTransactionData.isVisit()) {
                topologicalSortingHelper(childTransactionData);
            }

        }
        //making the vertex visited for future reference
        parentTransactionData.setVisit(true);

        //pushing to the stack as departing
        topologicalOrderedGraph.addLast(parentTransactionData);
    }

    public List<TccInfo> getTccConfirmedTransactions() {
        List<TccInfo> transactionConsensusConfirmed = new LinkedList<>();
        for (TransactionData transaction : topologicalOrderedGraph) {
            setTotalTrustScore(transaction);
            if (transaction.getTrustChainTrustScore() >= threshold) {
                transaction.setTrustChainConsensus(true);
                transaction.setTransactionConsensusUpdateTime(new Date());
                TccInfo tccInfo = new TccInfo(transaction.getHash(), transaction.getTrustChainTransactionHashes()
                        , transaction.getTrustChainTrustScore());

                transactionConsensusConfirmed.add(tccInfo);
                log.debug("transaction with hash:{} is confirmed with trustScore: {} and totalTrustScore:{} ", transaction.getHash(), transaction.getSenderTrustScore(), transaction.getTrustChainTrustScore());
            }
        }

        return transactionConsensusConfirmed;
    }

}
