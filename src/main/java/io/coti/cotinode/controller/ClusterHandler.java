package io.coti.cotinode.controller;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

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
        SourceSelector.SetSourceMap(sourceList);
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

}
