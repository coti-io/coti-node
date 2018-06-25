package io.coti.cotinode.service.interfaces;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import io.coti.cotinode.data.Hash;
import io.coti.cotinode.data.TransactionData;
import org.springframework.stereotype.Component;

public interface ICluster {
    // Test only!
    void initClusterFromTransactionList(List<TransactionData> allClusterTransactions) throws InterruptedException;

    void initCluster(List<Hash> notConfirmTransactions)  throws InterruptedException;

    boolean addNewTransaction(TransactionData transaction);

}
