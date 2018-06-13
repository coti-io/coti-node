package io.coti.cotinode.services.interfaces;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import io.coti.cotinode.model.Transaction;
import org.springframework.stereotype.Component;

@Component
public interface ICluster {
    void initCluster(List<Transaction> unconfirmedTransactions);
    ConcurrentHashMap<byte[], Transaction> getUnconfirmedTransactions();
    void addUnconfirmedTransaction(Transaction transaction, byte[] hash);
    Transaction getTransaction(byte[] hash);
    void deleteTransaction(byte[] hash);
    void addNewTransaction(Transaction transaction);
    void updateParentsTotalSumScore(Transaction transaction, int sonsTotalTrustScore, List<byte[]> trustChainTransactionHashes);
    void attachToSource(Transaction newTransaction, Transaction source);
}
