package io.coti.cotinode.interfaces;

import java.util.Collection;
import java.util.List;

public interface ISourceList {

    public void SetSourceList(Collection<ITransaction> sources);
    public void SetSourceList(ISourceList sourceList);
    public List<ITransaction> getSources();
    public void add(ISourceList list);
    public void add(ITransaction source);
    public int size();
    public void addAll(List<ITransaction> iTransactions);
}