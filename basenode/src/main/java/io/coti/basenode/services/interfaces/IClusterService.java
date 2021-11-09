package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public interface IClusterService {

    void sortTrustChainConfirmationClusterByTopologicalOrder();

    void attachToCluster(TransactionData transactionData);

    void detachFromCluster(TransactionData transactionData);

    void selectSources(TransactionData transactionData);

    void addExistingTransactionOnInit(TransactionData transactionData);

    void addMissingTransactionOnInit(TransactionData transactionData, Set<Hash> trustChainUnconfirmedExistingTransactionHashes);

    void finalizeInit();

    long getTotalSources();

    Set<Hash> getTrustChainConfirmationTransactionHashes();

    ConcurrentHashMap<Hash, TransactionData> getCopyTrustChainConfirmationCluster();

    ArrayList<HashSet<Hash>> getSourceSetsByTrustScore();

    TransactionData updateTrustChainConfirmationCluster(TransactionData transactionData);
}
