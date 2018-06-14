package io.coti.cotinode.service.interfaces;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import io.coti.cotinode.model.Transaction;
import io.coti.cotinode.storage.Interfaces.IPersistenceProvider;
import org.springframework.stereotype.Component;

@Component
public interface ICluster {
    void initCluster(List<Transaction> allClusterTransactions);
    void addUnconfirmedTransaction(Transaction transaction, byte[] hash);
    boolean addNewTransaction(Transaction transaction);
    void updateParentsTotalSumScore(Transaction transaction, int sonsTotalTrustScore, List<byte[]> trustChainTransactionHashes);
    void attachToSource(Transaction newTransaction, Transaction source);
    List<Transaction> getAllSourceTransactions();
    void deleteTransactionFromHashToAllClusterTransactionsMapping(byte[] hash);
    void deleteTransactionFromHashToToUnconfirmedTransactionsMapping(byte[] hash);
    void deleteTrustScoreToSourceListMapping(byte[] hash, Transaction transaction );
}
