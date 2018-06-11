package io.coti.cotinode.model;

import io.coti.cotinode.storage.Interfaces.IPersistenceProvider;
import org.rocksdb.ColumnFamilyHandle;

public abstract class Collection {

    protected IPersistenceProvider provider;
    public static ColumnFamilyHandle columnFamilyHandle;

    public void init(){
        provider.init();
    }

}
