package io.coti.basenode.services;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.data.TransactionType;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.interfaces.IClusterHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Service
public class ClusterHelper implements IClusterHelper {

    private static final long MINIMUM_WAIT_TIME_IN_SECONDS = 5;
    private static final long WAIT_REDUCTION_FACTOR = 10;
    @Autowired
    private Transactions transactions;

    private void addMissingChildIntoCluster(ConcurrentHashMap<Hash, TransactionData> trustChainConfirmationCluster,
                                            Hash child) {
        //trustChainConfirmationCluster.computeIfAbsent(child, c -> trustChainConfirmationCluster.put(c, transactions.getByHash(child)));
        if (!trustChainConfirmationCluster.containsKey(child))
        {
            trustChainConfirmationCluster.put(child, transactions.getByHash(child));
        }
    }

    private void populateSourceMaps(TransactionData oldestNonZeroSpendRoot, ConcurrentHashMap<TransactionData, TransactionData> rootSourcePairs,
                                    TransactionData transactionData, List<TransactionData> orphanedZeroSpendSources) {
        if (oldestNonZeroSpendRoot != null) {
            rootSourcePairs.put(oldestNonZeroSpendRoot, transactionData);
        } else {
            orphanedZeroSpendSources.add(transactionData);
        }
    }

    private TransactionData setOldestRoot(TransactionData transactionData, ConcurrentHashMap<Hash,
            TransactionData> trustChainConfirmationCluster, TransactionData oldestNonZeroSpendRoot) {
        boolean isZeroSpendTx = transactionData.getType().equals(TransactionType.ZeroSpend);
        boolean parentInCluster = isParentInCluster(transactionData, trustChainConfirmationCluster);
        if ((!parentInCluster || oldestNonZeroSpendRoot == null) && !isZeroSpendTx) {
            return transactionData;
        }
        return oldestNonZeroSpendRoot;
    }

    public boolean isParentInCluster(TransactionData transactionData, ConcurrentMap<Hash, TransactionData> trustChainConfirmationCluster) {
        boolean isLeftParentInCluster = transactionData.getLeftParentHash() != null && trustChainConfirmationCluster.containsKey(transactionData.getLeftParentHash());
        boolean isRightParentInCluster = transactionData.getRightParentHash() != null && trustChainConfirmationCluster.containsKey(transactionData.getRightParentHash());
        return isLeftParentInCluster || isRightParentInCluster;
    }

    public void mapPathsToSources(TransactionData transactionData, ConcurrentHashMap<Hash, TransactionData> trustChainConfirmationCluster,
                                  TransactionData oldestNonZeroSpendRoot, ConcurrentHashMap<TransactionData, TransactionData> rootSourcePairs,
                                  List<TransactionData> orphanedZeroSpendSources) {

        oldestNonZeroSpendRoot = setOldestRoot(transactionData, trustChainConfirmationCluster, oldestNonZeroSpendRoot);

        List<Hash> children = transactionData.getChildrenTransactionHashes();
        if (children.isEmpty()) {
            populateSourceMaps(oldestNonZeroSpendRoot, rootSourcePairs, transactionData, orphanedZeroSpendSources);
        } else {
            mapChildPaths(trustChainConfirmationCluster, oldestNonZeroSpendRoot, rootSourcePairs, orphanedZeroSpendSources, children);
        }
    }

    private void mapChildPaths(ConcurrentHashMap<Hash, TransactionData> trustChainConfirmationCluster, TransactionData oldestNonZeroSpendRoot, ConcurrentHashMap<TransactionData, TransactionData> rootSourcePairs, List<TransactionData> orphanedZeroSpendSources, List<Hash> children) {
        TransactionData majorChild = null;
        double maxTrustChainTrustScore = 0;
        List<TransactionData> minorChildren = new ArrayList<>();
        for (Hash child : children) {
            addMissingChildIntoCluster(trustChainConfirmationCluster, child);
            TransactionData childTransactionData = trustChainConfirmationCluster.get(child);
            if (oldestNonZeroSpendRoot != null) {
                if (childTransactionData.getTrustChainTrustScore() > maxTrustChainTrustScore) {
                    if (majorChild != null) {
                        minorChildren.add(majorChild);
                    }
                    maxTrustChainTrustScore = childTransactionData.getTrustChainTrustScore();
                    majorChild = childTransactionData;
                }
            } else {
                mapPathsToSources(childTransactionData, trustChainConfirmationCluster, null, rootSourcePairs, orphanedZeroSpendSources);
            }
        }
        if (majorChild != null) {
            mapPathsToSources(majorChild, trustChainConfirmationCluster, oldestNonZeroSpendRoot, rootSourcePairs, orphanedZeroSpendSources);
        }
        if (!minorChildren.isEmpty()) {
            for (TransactionData minorChild : minorChildren) {
                mapPathsToSources(minorChild, trustChainConfirmationCluster, null, rootSourcePairs, orphanedZeroSpendSources);
            }
        }
    }

    @Override
    public void sortByTopologicalOrder(ConcurrentMap<Hash, TransactionData> trustChainConfirmationCluster, LinkedList<TransactionData> topologicalOrderedGraph) {
        Map<Hash, TransactionData> childrenToAdd = new ConcurrentHashMap<>();
        trustChainConfirmationCluster.forEach((hash, transactionData) -> {
            transactionData.setVisit(false);
            transactionData.getChildrenTransactionHashes().forEach(childHash -> {
                if (!trustChainConfirmationCluster.containsKey(childHash)) {
                    TransactionData childTransaction = transactions.getByHash(childHash);
                    if (childTransaction == null) {
                        log.error("Child {} of transaction {} is not in cluster", childHash, transactionData.getHash());
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
                topologicalSortingHelper(transactionData, trustChainConfirmationCluster, topologicalOrderedGraph);
            }
        });
    }

    private void topologicalSortingHelper(TransactionData parentTransactionData, ConcurrentMap<Hash, TransactionData> trustChainConfirmationCluster, LinkedList<TransactionData> topologicalOrderedGraph) {
        for (Hash transactionDataHash : parentTransactionData.getChildrenTransactionHashes()) {
            TransactionData childTransactionData = trustChainConfirmationCluster.get(transactionDataHash);
            if (childTransactionData != null && !childTransactionData.isVisit()) {
                topologicalSortingHelper(childTransactionData, trustChainConfirmationCluster, topologicalOrderedGraph);
            }

        }
        //making the vertex visited for future reference
        parentTransactionData.setVisit(true);

        //pushing to the stack as departing
        topologicalOrderedGraph.addLast(parentTransactionData);
    }

    @Override
    public long getMinimumWaitTimeInMilliseconds(TransactionData transactionData) {
        return (long) ((100 - transactionData.getSenderTrustScore()) / WAIT_REDUCTION_FACTOR + MINIMUM_WAIT_TIME_IN_SECONDS) * 1000;
    }
}
