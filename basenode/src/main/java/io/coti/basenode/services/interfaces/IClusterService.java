package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public interface IClusterService {

    void attachToCluster(TransactionData transactionData);

    void updateTransactionOnTrustChainConfirmationCluster(TransactionData transactionData);

    void detachFromCluster(TransactionData transactionData);

    void selectSources(TransactionData transactionData);

    void addExistingTransactionOnInit(TransactionData transactionData);

    void addMissingTransactionOnInit(TransactionData transactionData, Set<Hash> trustChainUnconfirmedExistingTransactionHashes);

    void startToCheckTrustChainConfirmation();

    long getTotalSources();

    Set<Hash> getTrustChainConfirmationTransactionHashes();

    ConcurrentHashMap<Hash, TransactionData> getCopyTrustChainConfirmationCluster();

    ArrayList<HashSet<Hash>> getSourceSetsByTrustScore();

    void checkForTrustChainConfirmedTransaction();

    double getRuntimeTrustChainTrustScore(Hash transactionHash);

    List<TransactionData> findSources(TransactionData transactionData);

    void addTransactionToTrustChainConfirmationCluster(TransactionData transactionData);

    Object getSourcesStarvationCheckLock();

    Map<Hash, Double> getTransactionTrustChainTrustScoreMap();

    LinkedList<TransactionData> getTopologicalOrderedGraph();
}
