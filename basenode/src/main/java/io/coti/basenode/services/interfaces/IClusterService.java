package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;

import java.util.List;
import java.util.Set;

public interface IClusterService {

    List<Set<TransactionData>> getSourceListsByTrustScore();

    void attachToCluster(TransactionData transactionData);

    void selectSources(TransactionData transactionData);

    void addExistingTransactionOnInit(TransactionData transactionData);

    void addMissingTransactionOnInit(TransactionData transactionData, Set<Hash> trustChainUnconfirmedExistingTransactionHashes);

    void finalizeInit();

    long getTotalSources();

    Set<Hash> getTrustChainConfirmationTransactionHashes();
}
