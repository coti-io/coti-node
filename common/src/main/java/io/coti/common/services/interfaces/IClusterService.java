package io.coti.common.services.interfaces;

import io.coti.common.data.TransactionData;

public interface IClusterService {

    void attachToCluster(TransactionData transactionData);

    void selectSources(TransactionData transactionData);

    void addUnconfirmedTransaction(TransactionData transactionData);

    void finalizeInit();

    long getTotalSources();
}
