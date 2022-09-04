package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

public interface IClusterHelper {

    void mapPathsToSources(TransactionData transactionData, ConcurrentMap<Hash, TransactionData> trustChainConfirmationCluster, TransactionData oldestRootAncestor, ConcurrentMap<TransactionData, TransactionData> rootSourcePairs, List<TransactionData> orphanedZeroSpendSources);

    void sortByTopologicalOrder(ConcurrentMap<Hash, TransactionData> trustChainConfirmationCluster, LinkedList<TransactionData> topologicalOrderedGraph);

    long getMinimumWaitTimeInMilliseconds(TransactionData transactionData);

    void addSubTreeDescendants(ConcurrentMap<Hash, TransactionData> trustChainConfirmationCluster);

    void addAndSortByTopologicalOrder(ConcurrentMap<Hash, TransactionData> trustChainConfirmationCluster, LinkedList<TransactionData> topologicalOrderedGraph);

    boolean isParentInCluster(TransactionData transactionData, ConcurrentMap<Hash, TransactionData> trustChainConfirmationCluster);
}
