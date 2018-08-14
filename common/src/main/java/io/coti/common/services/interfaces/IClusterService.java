package io.coti.common.services.interfaces;

import io.coti.common.data.Hash;
import io.coti.common.data.TransactionData;

import java.util.List;
import java.util.Vector;

public interface IClusterService {

    TransactionData attachToCluster(TransactionData transactionData);

    TransactionData selectSources(TransactionData transactionData);

    void setInitialUnconfirmedTransactions(List<Hash> transactionHashes);

    Vector<TransactionData>[] getSourceListsByTrustScore();

}
