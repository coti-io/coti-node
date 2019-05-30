package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

public interface IClusterHelper {

    void sortByTopologicalOrder(ConcurrentHashMap<Hash, TransactionData> trustChainConfirmationCluster, LinkedList<TransactionData> topologicalOrderedGraph);

}
