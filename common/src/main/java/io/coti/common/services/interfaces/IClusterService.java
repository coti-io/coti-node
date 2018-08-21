package io.coti.common.services.interfaces;

import io.coti.common.data.TransactionData;

public interface IClusterService {

    TransactionData attachToCluster(TransactionData transactionData);

    TransactionData selectSources(TransactionData transactionData);

    void addUnconfirmedTransaction(TransactionData transactionData);

    void finalizeInit();

    long getTotalSources();
}
