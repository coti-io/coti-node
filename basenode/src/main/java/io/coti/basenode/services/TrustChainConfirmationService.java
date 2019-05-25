package io.coti.basenode.services;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TccInfo;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.model.Transactions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@Configurable
public class TrustChainConfirmationService {
    @Value("${cluster.trust.chain.threshold}")
    private int threshold;
    private ConcurrentHashMap<Hash, TransactionData> trustChainConfirmationCluster;
    private LinkedList<TransactionData> topologicalOrderedGraph;
    @Autowired
    private Transactions transactions;

    public void init(ConcurrentHashMap<Hash, TransactionData> trustChainConfirmationCluster) {
        this.trustChainConfirmationCluster = new ConcurrentHashMap<>(trustChainConfirmationCluster);
        topologicalOrderedGraph = new LinkedList<>();
        sortByTopologicalOrder();
    }

    private void sortByTopologicalOrder() {
        Map<Hash, TransactionData> childrenToAdd = new ConcurrentHashMap<>();
        trustChainConfirmationCluster.forEach((hash, transactionData) -> {
            transactionData.setVisit(false);
            transactionData.getChildrenTransactionHashes().forEach(childHash -> {
                if (!trustChainConfirmationCluster.containsKey(childHash)) {
                    TransactionData childTransaction = transactions.getByHash(childHash);
                    if (childTransaction != null) {
                        log.error("Child {} of trasnaction {} is not in cluster", childHash, transactionData.getHash());
                    } else {
                        childTransaction.setVisit(false);
                        childrenToAdd.put(childHash, childTransaction);
                    }
                }
            });
        });
        trustChainConfirmationCluster.putAll(childrenToAdd);

        //loop is for making sure that every vertex is visited since if we select only one random source
        //all vertices might not be reachable from this source
        //eg:1->2->3,1->3 and if we select 3 as source first then no vertex can be visited of course except for 3
        trustChainConfirmationCluster.forEach((hash, transactionData) -> {
            if (!transactionData.isVisit()) {
                topologicalSortingHelper(transactionData);
            }
        });
    }

    private void setTotalTrustScore(TransactionData parent) {
        double maxSonsTotalTrustScore = 0;

        for (Hash transactionHash : parent.getChildrenTransactionHashes()) {
            try {
                TransactionData child = trustChainConfirmationCluster.get(transactionHash);
                if (child != null && child.getTrustChainTrustScore()
                        > maxSonsTotalTrustScore) {
                    maxSonsTotalTrustScore = trustChainConfirmationCluster.get(transactionHash).getTrustChainTrustScore();
                }
            } catch (Exception e) {
                log.error("in setTotalSumScore: parent: {} child: {}", parent.getHash(), transactionHash);
                throw e;
            }
        }

        // updating parent trustChainTrustScore
        if (parent.getTrustChainTrustScore() < parent.getSenderTrustScore() + maxSonsTotalTrustScore) {
            parent.setTrustChainTrustScore(parent.getSenderTrustScore() + maxSonsTotalTrustScore);
        }

    }

    private void topologicalSortingHelper(TransactionData parentTransactionData) {
        for (Hash transactionDataHash : parentTransactionData.getChildrenTransactionHashes()) {
            TransactionData childTransactionData = trustChainConfirmationCluster.get(transactionDataHash);
            if (childTransactionData != null && !childTransactionData.isVisit()) {
                topologicalSortingHelper(childTransactionData);
            }

        }
        //making the vertex visited for future reference
        parentTransactionData.setVisit(true);

        //pushing to the stack as departing
        topologicalOrderedGraph.addLast(parentTransactionData);
    }

    public List<TccInfo> getTrustChainConfirmedTransactions() {
        LinkedList<TccInfo> trustChainConfirmations = new LinkedList<>();
        for (TransactionData transactionData : topologicalOrderedGraph) {
            setTotalTrustScore(transactionData);
            if (transactionData.getTrustChainTrustScore() >= threshold && !transactionData.isTrustChainConsensus()) {
                Instant trustScoreConsensusTime = Optional.ofNullable(transactionData.getTrustChainConsensusTime()).orElse(Instant.now());
                TccInfo tccInfo = new TccInfo(transactionData.getHash(), transactionData.getTrustChainTrustScore(), trustScoreConsensusTime);
                trustChainConfirmations.addFirst(tccInfo);
                log.debug("transaction with hash:{} is confirmed with trustScore: {} and totalTrustScore:{} ", transactionData.getHash(), transactionData.getSenderTrustScore(), transactionData.getTrustChainTrustScore());
            }
        }

        return trustChainConfirmations;
    }

}
