package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.TransactionData;

import java.util.List;

public interface IClusterService {

    List<List<TransactionData>> getSourceListsByTrustScore();

    void attachToCluster(TransactionData transactionData);

    void selectSources(TransactionData transactionData);

    void addUnconfirmedTransaction(TransactionData transactionData);

    void finalizeInit();

    long getTotalSources();
}
