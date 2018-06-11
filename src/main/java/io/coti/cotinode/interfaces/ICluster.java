package io.coti.cotinode.interfaces;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public interface ICluster {
    public void initCluster(ConcurrentHashMap<String, ITransaction> transactions);
    public ConcurrentHashMap<String, ITransaction> getTransactions();
    public void addTransaction(ITransaction t, String key);
    public ITransaction getTransaction(String key);
    public void setTransactions(ConcurrentHashMap<String, ITransaction> transactions);
    public void deleteTransaction(String key);

    void setSourceList(ConcurrentHashMap<String, ISourceList> sourceList);
}
