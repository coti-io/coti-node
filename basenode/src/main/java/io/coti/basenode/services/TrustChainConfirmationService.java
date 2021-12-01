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
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Service
@Configurable
public class TrustChainConfirmationService {

    @Value("${cluster.trust.chain.threshold}")
    private int threshold;
    @Value("${force.dspc.for.tcc.transaction.index: 700000}")
    protected long forceDSPCForTCCThreshold;
    private ConcurrentMap<Hash, TransactionData> trustChainConfirmationCluster;
    private LinkedList<TransactionData> topologicalOrderedGraph;
    @Autowired
    private IClusterHelper clusterHelper;
    @Autowired
    private Transactions transactions;
    @Autowired
    private TransactionIndexService transactionIndexService;

    public void init(ConcurrentMap<Hash, TransactionData> trustChainConfirmationCluster) {
        this.trustChainConfirmationCluster = new ConcurrentHashMap<>(trustChainConfirmationCluster);
        topologicalOrderedGraph = new LinkedList<>();
        clusterHelper.sortByTopologicalOrder(trustChainConfirmationCluster, topologicalOrderedGraph);
    }

    private double getSenderTrustScore(TransactionData transactionData) {
        if (isForceDSPCForTCC(transactionData)) {
            return transactionData.getSenderTrustScore();
        }
        return 0;
    }

    private boolean isForceDSPCForTCC(TransactionData transactionData) {
        boolean isRuleActive =  RulesConditionsService.ADDAX_FORK.isRuleApplicable(transactionIndexService, Long.valueOf(forceDSPCForTCCThreshold));
        if (isRuleActive)
            log.debug("Rule for ADDAX_FORK on");
        return !isRuleActive || isDspConsensus(transactionData, true);
    }

    public boolean isDspConsensus(TransactionData transactionData, boolean checkInDB) {
        boolean retValue = false;
        if (transactionData.isDspConsensus())
            retValue = true;
        if (!retValue && checkInDB) {
            TransactionData transactionsByHash = transactions.getByHash(transactionData.getHash());
            if (transactionsByHash != null && transactionsByHash.getDspConsensusResult() != null) {
                retValue =  transactionsByHash.getDspConsensusResult().isDspConsensus();
            } else {
                retValue = transactionData.isDspConsensus();
            }
        }
        return retValue;
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

        if (parent.getTrustChainTrustScore() < getSenderTrustScore(parent) + maxChildrenTotalTrustScore) {
            parent.setTrustChainTrustScore(getSenderTrustScore(parent) + maxChildrenTotalTrustScore);
        }
    }

    public List<TccInfo> getTrustChainConfirmedTransactions() {
        LinkedList<TccInfo> trustChainConfirmations = new LinkedList<>();
        for (TransactionData transactionData : topologicalOrderedGraph) {
            setTotalTrustScore(transactionData);
            if (isForceDSPCForTCC(transactionData) && transactionData.getTrustChainTrustScore() >= threshold && !transactionData.isTrustChainConsensus()) {
                Instant trustScoreConsensusTime = Optional.ofNullable(transactionData.getTrustChainConsensusTime()).orElse(Instant.now());
                TccInfo tccInfo = new TccInfo(transactionData.getHash(), transactionData.getTrustChainTrustScore(), trustScoreConsensusTime);
                trustChainConfirmations.addFirst(tccInfo);
                log.debug("transaction with hash:{} is confirmed with trustScore: {} and totalTrustScore:{} ", transactionData.getHash(), transactionData.getSenderTrustScore(), transactionData.getTrustChainTrustScore());
            }
        }
        return trustChainConfirmations;
    }

}
