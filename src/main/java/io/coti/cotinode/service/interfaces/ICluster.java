package io.coti.cotinode.service.interfaces;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import io.coti.cotinode.data.Hash;
import io.coti.cotinode.data.TransactionData;
import org.springframework.stereotype.Component;

public interface ICluster {
    void initCluster(List<TransactionData> allClusterTransactions) throws InterruptedException;

    void addToUnTccConfirmedTransactionMap(TransactionData transaction);

    void addToTrustScoreToSourceListMap(TransactionData transaction);

    boolean addNewTransaction(TransactionData transaction);

    List<TransactionData> getNewTransactions();

    void attachToSource(TransactionData newTransaction, TransactionData source);

    List<TransactionData> getAllSourceTransactions();

    void trustScoreConsensusProcess();

    void deleteTransactionFromHashToUnTccConfirmedTransactionsMapping(Hash hash);

    void deleteTrustScoreToSourceListMapping(TransactionData transaction);
}
