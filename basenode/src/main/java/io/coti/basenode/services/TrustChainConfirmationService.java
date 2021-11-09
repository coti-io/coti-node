package io.coti.basenode.services;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TccInfo;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.interfaces.IClusterHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Service
@Configurable
public class TrustChainConfirmationService {

    @Value("${cluster.trust.chain.threshold}")
    private int threshold;
    private ConcurrentMap<Hash, TransactionData> trustChainConfirmationCluster;
    private LinkedList<TransactionData> topologicalOrderedGraph;
    @Autowired
    private IClusterHelper clusterHelper;
    @Autowired
    private Transactions transactions;

    public void init(ConcurrentMap<Hash, TransactionData> trustChainConfirmationCluster) {
        this.trustChainConfirmationCluster = new ConcurrentHashMap<>(trustChainConfirmationCluster);
        topologicalOrderedGraph = new LinkedList<>();
        clusterHelper.sortByTopologicalOrder(trustChainConfirmationCluster, topologicalOrderedGraph);
    }

    private void setTotalTrustScore(TransactionData parent) {
        double maxSonTotalTrustScore = getMaxSonTotalTrustScore(parent);

        // updating parent trustChainTrustScore
        if (parent.getTrustChainTrustScore() < parent.getSenderTrustScore() + maxSonTotalTrustScore) {
            parent.setTrustChainTrustScore(parent.getSenderTrustScore() + maxSonTotalTrustScore);
        }
    }

    public double getMaxSonTotalTrustScore(TransactionData parent) {
        double maxSonTotalTrustScore = 0;
        for (Hash childTransactionHash : parent.getChildrenTransactionHashes()) {
            try {
                TransactionData child = trustChainConfirmationCluster.get(childTransactionHash);
                if (child == null) {
                    child = transactions.getByHash(childTransactionHash);
                    if (child == null) {
                        throw new NoSuchElementException(String.format("Transaction expected child %s is missing in DB", childTransactionHash));
                    }
                }
                if (child.getTrustChainTrustScore() > maxSonTotalTrustScore) {
                    maxSonTotalTrustScore = trustChainConfirmationCluster.get(childTransactionHash).getTrustChainTrustScore();
                }
            } catch (Exception e) {
                log.error("in setTotalSumScore: parent: {} child: {}", parent.getHash(), childTransactionHash);
                throw e;
            }
        }
        return maxSonTotalTrustScore;
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

    public void updateTrustChainClusterTransactions() {
        for (TransactionData transactionData : topologicalOrderedGraph) {
            setTotalTrustScore(transactionData);
        }
    }

    public int getThreshold() {
        return threshold;
    }

    public TransactionData updateTrustChainConfirmationCluster(TransactionData transactionData) {
        return trustChainConfirmationCluster.put(transactionData.getHash(), transactionData);
    }

}
