package io.coti.cotinode.interfaces;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public interface IClusterHandler {

    public void init(ConcurrentHashMap<String, ITransaction> transactions);
    public ConcurrentHashMap<String, ITransaction> getTransactions();
    public void addTransaction(ITransaction t, String key);
    public ITransaction getTransaction(String key);
    public void setTransactions(ConcurrentHashMap<String, ITransaction> transactions);
    public void deleteTransaction(String key);
    public Collection<ITransaction> getSorurceTransactions(ConcurrentHashMap<String, ITransaction> transactions);
    public void setNewTransaction(ITransaction transaction);
    public void updateParentsTotalSumScore(ITransaction transaction, int sonsTotalTrustScore);
}
