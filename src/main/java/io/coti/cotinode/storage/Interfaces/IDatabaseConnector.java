package io.coti.cotinode.storage.Interfaces;

import io.coti.cotinode.data.Hash;
import io.coti.cotinode.data.IEntity;
import io.coti.cotinode.data.TransactionData;
import io.coti.cotinode.model.*;
import org.rocksdb.RocksIterator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

public interface IDatabaseConnector {

    boolean put(String columnFamilyName, IEntity IEntity);

    void shutdown();

    byte[] getByHash(String columnFamilyName, Hash hash);

    void delete(String columnFamilyName, Hash hash);

    public Map<Object, Object> getFullMapFromDB(String coulumnFamilyName);

    public RocksIterator getLastElementIteratorFromColumnFamily(String columnFamilyName);

    public Map<Object,Object> getMapAfterKeyFromDB(String columnFamilyName, Object key);
}
