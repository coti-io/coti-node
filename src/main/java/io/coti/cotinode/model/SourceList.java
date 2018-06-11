package io.coti.cotinode.model;

import java.util.Vector;
import java.util.Collection;
import java.util.List;

import io.coti.cotinode.interfaces.ISourceList;
import io.coti.cotinode.interfaces.ITransaction;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SourceList implements ISourceList {

    List<ITransaction> sources = new Vector<>();

    @Override
    public void SetSourceList(Collection<ITransaction> sources) {
        this.sources.addAll(sources);
    }

    @Override
    public void SetSourceList(ISourceList sourceList) {
        sources = new Vector<>(sourceList.getSources());
    }

    @Override
    public List<ITransaction> getSources() {
        return sources;
    }

    @Override
    public void add(ISourceList list) {
        sources.addAll(list.getSources());
    }

    @Override
    public void add(ITransaction source) {
        sources.add(source);
    }

    @Override
    public void addAll(List<ITransaction> iTransactions)
    {
        sources.addAll(iTransactions);
    }


    @Override
    public int size() {
        return sources.size();
    }

    @Override
    public String toString() {
        return "SourceList{" +
                "sources=" + sources +
                "}";
    }
}
