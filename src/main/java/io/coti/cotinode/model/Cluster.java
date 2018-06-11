package io.coti.cotinode.model;

import java.util.concurrent.ConcurrentHashMap;

import io.coti.cotinode.interfaces.ICluster;
import io.coti.cotinode.interfaces.ISourceList;
import io.coti.cotinode.interfaces.ITransaction;

import org.springframework.stereotype.Component;

@Component
public class Cluster implements ICluster {
    private ConcurrentHashMap<String, ITransaction> transactions;

    private ConcurrentHashMap<String, ISourceList> sourceList;

    @Override
    public void initCluster(ConcurrentHashMap<String, ITransaction> transactions){
        setTransactions(transactions);
    }

    public void initSourceList(ConcurrentHashMap<String, ISourceList> sourceList){
        setSourceList(sourceList);
    }

    @Override
    public void setTransactions(ConcurrentHashMap<String, ITransaction> transactions) {
        this.transactions = transactions;
    }

    @Override
    public ConcurrentHashMap<String, ITransaction> getTransactions() {
        return transactions;
    }

    @Override
    public void addTransaction(ITransaction t, String key) {
        transactions.put(key, t);
    }

    @Override
    public ITransaction getTransaction(String key) {
        return transactions.get(key);
    }

    @Override
    public void deleteTransaction(String key) {
        transactions.remove(key);
    }

    @Override
    public void setSourceList(ConcurrentHashMap<String, ISourceList> sourceList) {
        this.sourceList = sourceList;
    }

}

