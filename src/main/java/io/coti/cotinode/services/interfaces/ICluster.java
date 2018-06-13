package io.coti.cotinode.services.interfaces;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import io.coti.cotinode.model.Interfaces.ITransaction;
import org.springframework.stereotype.Component;

@Component
public interface ICluster {
    void initCluster(List<ITransaction> unconfirmedTransactions);
    ConcurrentHashMap<byte[], ITransaction> getUnconfirmedTransactions();
    void addUnconfirmedTransaction(ITransaction transaction, byte[] hash);
    ITransaction getTransaction(byte[] hash);
    void deleteTransaction(byte[] hash);
    void addNewTransaction(ITransaction transaction);
    void updateParentsTotalSumScore(ITransaction transaction, int sonsTotalTrustScore);
    void attachToSource(ITransaction newTransaction, ITransaction source);
}
