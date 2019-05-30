package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public interface IClusterService {

    void attachToCluster(TransactionData transactionData);

    void selectSources(TransactionData transactionData);

    void addExistingTransactionOnInit(TransactionData transactionData);

    void addMissingTransactionOnInit(TransactionData transactionData, Set<Hash> trustChainUnconfirmedExistingTransactionHashes);

    void finalizeInit();

    long getTotalSources();

    Set<Hash> getTrustChainConfirmationTransactionHashes();

    ConcurrentHashMap<Hash, TransactionData> getCopyTrustChainConfirmationCluster();
}
