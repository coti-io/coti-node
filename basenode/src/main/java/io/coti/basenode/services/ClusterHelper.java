package io.coti.basenode.services;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.interfaces.IClusterHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class ClusterHelper implements IClusterHelper {
    @Autowired
    private Transactions transactions;

    @Override
    public void sortByTopologicalOrder(ConcurrentHashMap<Hash, TransactionData> trustChainConfirmationCluster, LinkedList<TransactionData> topologicalOrderedGraph) {
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

    private void topologicalSortingHelper(TransactionData parentTransactionData, ConcurrentHashMap<Hash, TransactionData> trustChainConfirmationCluster, LinkedList<TransactionData> topologicalOrderedGraph) {
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
}
