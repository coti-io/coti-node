package io.coti.basenode.services;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TccInfo;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.data.TransactionType;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static io.coti.basenode.services.BaseNodeServiceManager.clusterHelper;

@Slf4j
@Service
@Configurable
public class TrustChainConfirmationService {

    @Value("${cluster.trust.chain.threshold}")
    private int threshold;
    private ConcurrentMap<Hash, TransactionData> trustChainConfirmationCluster;
    private LinkedList<TransactionData> topologicalOrderedGraph;
    private final Map<Hash, Double> nonZeroSpendTransactionTSValuesMap = new HashMap<>();
    @Getter
    private long numberOfTimesTrustScoreNotChanged = 0;
    private boolean trustScoreNotChanged = false;
    @Getter
    private int tccWaitingConfirmation = 0;

    public void init(ConcurrentMap<Hash, TransactionData> trustChainConfirmationCluster) {
        this.trustChainConfirmationCluster = new ConcurrentHashMap<>(trustChainConfirmationCluster);
        topologicalOrderedGraph = new LinkedList<>();
        clusterHelper.sortByTopologicalOrder(this.trustChainConfirmationCluster, topologicalOrderedGraph);
    }

    private void setTotalTrustScore(TransactionData parent) {
        double maxChildrenTotalTrustScore = 0;

        for (Hash transactionHash : parent.getChildrenTransactionHashes()) {
            try {
                TransactionData child = trustChainConfirmationCluster.get(transactionHash);
                if (child != null && child.getTrustChainTrustScore()
                        > maxChildrenTotalTrustScore) {
                    maxChildrenTotalTrustScore = child.getTrustChainTrustScore();
                }
            } catch (Exception e) {
                log.error("in setTotalSumScore: parent: {} child: {}", parent.getHash(), transactionHash);
                throw e;
            }
        }

        // updating parent trustChainTrustScore
        if (parent.getTrustChainTrustScore() < parent.getSenderTrustScore() + maxChildrenTotalTrustScore) {
            parent.setTrustChainTrustScore(parent.getSenderTrustScore() + maxChildrenTotalTrustScore);
        }
    }

    public List<TccInfo> getTrustChainConfirmedTransactions() {
        LinkedList<TccInfo> trustChainConfirmations = new LinkedList<>();
        trustScoreNotChanged = false;
        for (TransactionData transactionData : topologicalOrderedGraph) {
            setTotalTrustScore(transactionData);
            if (transactionData.getTrustChainTrustScore() >= threshold && !transactionData.isTrustChainConsensus()) {
                nonZeroSpendTransactionTSValuesMap.remove(transactionData.getHash());
                Instant trustScoreConsensusTime = Optional.ofNullable(transactionData.getTrustChainConsensusTime()).orElse(Instant.now());
                TccInfo tccInfo = new TccInfo(transactionData.getHash(), transactionData.getTrustChainTrustScore(), trustScoreConsensusTime);
                trustChainConfirmations.addFirst(tccInfo);
                log.debug("transaction with hash:{} is confirmed with trustScore: {} and totalTrustScore:{} ", transactionData.getHash(), transactionData.getSenderTrustScore(), transactionData.getTrustChainTrustScore());
            } else {
                monitorTotalTrustScore(transactionData);
            }
        }
        updateMonitorState();

        return trustChainConfirmations;
    }

    private void updateMonitorState() {
        if (trustScoreNotChanged) {
            numberOfTimesTrustScoreNotChanged++;
        } else {
            numberOfTimesTrustScoreNotChanged = 0;
        }
    }

    private void monitorTotalTrustScore(TransactionData transactionData) {
        if (transactionData.getType().equals(TransactionType.ZeroSpend)) {
            return;
        }

        if (nonZeroSpendTransactionTSValuesMap.containsKey(transactionData.getHash()) &&
                nonZeroSpendTransactionTSValuesMap.get(transactionData.getHash()) >= transactionData.getTrustChainTrustScore() &&
                isWaitingMoreThanMinimum(transactionData)) {
            trustScoreNotChanged = true;
        }

        nonZeroSpendTransactionTSValuesMap.put(transactionData.getHash(), transactionData.getTrustChainTrustScore());
    }

    private boolean isWaitingMoreThanMinimum(TransactionData transactionData) {
        long minimumWaitingTimeInMilliseconds = clusterHelper.getMinimumWaitTimeInMilliseconds(transactionData);
        long actualWaitingTimeInMilliseconds = Duration.between(transactionData.getAttachmentTime(), Instant.now()).toMillis();
        return actualWaitingTimeInMilliseconds > minimumWaitingTimeInMilliseconds;
    }

}
