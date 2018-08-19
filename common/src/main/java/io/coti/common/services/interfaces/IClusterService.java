package io.coti.common.services.interfaces;

import io.coti.common.data.TransactionData;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
public interface IClusterService {

    TransactionData attachToCluster(TransactionData transactionData);

    TransactionData selectSources(TransactionData transactionData);

    void addUnconfirmedTransaction(TransactionData transactionData);

    List<List<TransactionData>> getSourceListsByTrustScore();

    void finalizeInit();
}
