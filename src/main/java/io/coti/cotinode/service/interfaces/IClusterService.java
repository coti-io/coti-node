package io.coti.cotinode.service.interfaces;

import io.coti.cotinode.data.Hash;
import io.coti.cotinode.data.TransactionData;

import java.util.List;

public interface IClusterService {

    void initCluster(List<Hash> notConfirmTransactions) throws InterruptedException;

    TransactionData addTransactionDataToSources(TransactionData zeroSpendTransaction);

    TransactionData selectSources(TransactionData transactionData);

}
