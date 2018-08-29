package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.TransactionData;

public interface IClusterService {

    void attachToCluster(TransactionData transactionData);

    void selectSources(TransactionData transactionData);

    void addUnconfirmedTransaction(TransactionData transactionData);

    void finalizeInit();

    long getTotalSources();
}
