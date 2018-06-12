package io.coti.cotinode.services;

import io.coti.cotinode.model.Interfaces.ITransaction;
import io.coti.cotinode.model.Transaction;
import io.coti.cotinode.services.interfaces.ICluster;
import io.coti.cotinode.storage.Interfaces.IPersistenceProvider;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class Cluster implements ICluster {
    private IPersistenceProvider persistenceProvider;
    private ConcurrentHashMap<byte[], ITransaction> hashToUnconfirmedTransactionsMapping;
    private ConcurrentHashMap<Integer, List<Transaction>> trustScoreToSourceListMapping;

    @Override
    public void initCluster(List<ITransaction> unconfirmedTransactions){
        hashToUnconfirmedTransactionsMapping = new ConcurrentHashMap<>();
        trustScoreToSourceListMapping = new ConcurrentHashMap<>();
        setUnconfirmedTransactions(unconfirmedTransactions);
    }

    private void setUnconfirmedTransactions(List<ITransaction> uncofirmedTransactinos) {
        this.hashToUnconfirmedTransactionsMapping.
                putAll(uncofirmedTransactinos.stream().
                        collect(Collectors.
                                toMap(ITransaction::getHash, Function.identity())));
    }

    @Override
    public ConcurrentHashMap<byte[], ITransaction> getUnconfirmedTransactions() {
        return hashToUnconfirmedTransactionsMapping;
    }

    @Override
    public void addUnconfirmedTransaction(ITransaction transaction, byte[] hash) {
        hashToUnconfirmedTransactionsMapping.put(hash, transaction);
    }

    @Override
    public ITransaction getTransaction(byte[] hash) {
        if(hashToUnconfirmedTransactionsMapping.containsKey(hash)){
            return hashToUnconfirmedTransactionsMapping.get(hash);
        }
        else{
            return persistenceProvider.getTransaction(hash);
        }
    }

    @Override
    public void deleteTransaction(byte[] hash) {
        if(hashToUnconfirmedTransactionsMapping.containsKey(hash)){
            hashToUnconfirmedTransactionsMapping.remove(hash);
        }
        else{
            persistenceProvider.deleteTransaction(hash);
        }
    }

    public List<ITransaction> getAllSourceTransactions() {
        return hashToUnconfirmedTransactionsMapping.values().stream().
                filter(ITransaction::isSource).collect(Collectors.toList());
    }

    @Override
    public void addNewTransaction(ITransaction transaction) {

        // Selection of sources
        List<ITransaction> randomWeightedSources = null;
        // TODO : add do the attachment

        // Update the total trust score of the parents
        updateParentsTotalSumScore(transaction, 0);

        // TODO : POW
    }

    @Override
    public void updateParentsTotalSumScore(ITransaction transaction, int sonsTotalTrustScore) {
        if (transaction != null && !transaction.isThresholdAchieved()) {
            if (transaction.getTotalTrustScore() <  sonsTotalTrustScore + transaction.getSenderTrustScore()) {
                transaction.setTotalTrustScore(sonsTotalTrustScore + transaction.getSenderTrustScore());
                if (transaction.getTotalTrustScore() >= 300 ) {// TODO : set the number as consant
                    transaction.setThresholdAchieved(true);
                    hashToUnconfirmedTransactionsMapping.remove(transaction.getKey());
                }
            }
            updateParentsTotalSumScore(transaction.getLeftParent(), transaction.getTotalTrustScore());
            updateParentsTotalSumScore(transaction.getRightParent(), transaction.getTotalTrustScore());
        }
    }

    public void attachToSource(ITransaction newTransaction, ITransaction source) {
        if(hashToUnconfirmedTransactionsMapping.get(source.getKey()) == null) {
            System.out.println("Cannot find source:" + source);
            throw new RuntimeException("Cannot find source:" + source);
        }
        newTransaction.attachToSource(source);
    }


}

