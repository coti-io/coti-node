package io.coti.cotinode.controller;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

import io.coti.cotinode.interfaces.*;
import io.coti.cotinode.model.SourceList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ClusterHandler implements IClusterHandler {

    @Autowired
    private ICluster cluster;

    @Autowired
    private ISourceSelector SourceSelector;

    @Override
    public void init(ConcurrentHashMap<String, ITransaction> transactions){
        cluster.initCluster(transactions);

        ISourceList sourceList = new SourceList();
        sourceList.SetSourceList(getSorurceTransactions(transactions));
        SourceSelector.setSourceMap(sourceList);
    }

    @Override
    public ConcurrentHashMap<String, ITransaction> getTransactions() {
        return cluster.getTransactions();
    }

    @Override
    public void addTransaction(ITransaction t, String key) {
        cluster.addTransaction(t, key);
    }

    @Override
    public ITransaction getTransaction(String key) {
        return cluster.getTransaction(key);
    }

    @Override
    public void setTransactions(ConcurrentHashMap<String, ITransaction> transactions) {
        cluster.setTransactions(transactions);
    }

    @Override
    public void deleteTransaction(String key) {
        cluster.deleteTransaction(key);
    }

    @Override
    public Collection<ITransaction> getSorurceTransactions(ConcurrentHashMap<String, ITransaction> transactions) {

        Collection<ITransaction> sourceTransactions = (Collection<ITransaction>) transactions.entrySet().stream()
                .filter(map -> map.getValue().getLeftParent() == null && map.getValue().getRightParent() == null);
        return sourceTransactions;
    }

    @Override
    public void setNewTransaction(ITransaction transaction) {

        // Selection of sources
        ISourceList randomWeightedSources = null;
        // TODO : add do the attachment

        // Update the total trust score of the parents
        updateParentsTotalSumScore(transaction, 0);

        // TODO : POW

    }

    @Override
    public void updateParentsTotalSumScore(ITransaction transaction, int sonsTotalTrustScore) {
        if (transaction != null && !transaction.getIsTreshHoledAchieved()) {
            if (transaction.getTotalWeight() <  sonsTotalTrustScore + transaction.getMyWeight()) {
                transaction.setTotalWeight(sonsTotalTrustScore + transaction.getMyWeight());
                if (transaction.getTotalWeight() >= 300 ) {// TODO : set the number as consant
                    transaction.setIsTreshHoledAchieved(true);
                    SourceSelector.delete(transaction);
                }
            }
            updateParentsTotalSumScore(transaction.getLeftParent(), transaction.getTotalWeight());
            updateParentsTotalSumScore(transaction.getRightParent(), transaction.getTotalWeight());
        }
    }

}
