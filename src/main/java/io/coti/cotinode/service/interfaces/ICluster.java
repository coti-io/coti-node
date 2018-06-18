package io.coti.cotinode.service.interfaces;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import io.coti.cotinode.data.Hash;
import io.coti.cotinode.data.TransactionData;
import org.springframework.stereotype.Component;

@Component
public interface ICluster {
    void initCluster(List<TransactionData> allClusterTransactions);
    void addToUnTccConfirmedTransactionMap(TransactionData transaction);
    void addToTrustScoreToSourceListMap(TransactionData transaction);
    boolean addNewTransaction(TransactionData transaction, boolean isFromPpropagation);
    void updateParentsTotalSumScore(TransactionData transaction, int sonsTotalTrustScore, List<Hash> trustChainTransactionHashes);
    void attachToSource(TransactionData newTransaction, TransactionData source);
    List<TransactionData> getAllSourceTransactions();
   // void deleteTransactionFromHashToAllClusterTransactionsMapping(Hash hash);
    void deleteTransactionFromHashToUnTccConfirmedTransactionsMapping(Hash hash);
    void deleteTrustScoreToSourceListMapping(Hash hash, TransactionData transaction );
}
