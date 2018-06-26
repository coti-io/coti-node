package io.coti.cotinode.service.interfaces;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import io.coti.cotinode.data.Hash;
import io.coti.cotinode.data.TransactionData;
import org.springframework.stereotype.Component;

public interface IClusterService {

    void initCluster(List<Hash> notConfirmTransactions)  throws InterruptedException;

    TransactionData addTransactionDataToSources(TransactionData zeroSpendTransaction);

    TransactionData selectSources(TransactionData transactionData);

    boolean isSourceListEmpty();
}
