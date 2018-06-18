package io.coti.cotinode.database.Interfaces;

import org.rocksdb.RocksIterator;

import java.util.Map;

public interface IDatabaseConnector {

    boolean put(String columnFamilyName, byte[] key, byte[] value);

    byte[] getByKey(String columnFamilyName, byte[] key);

    void delete(String columnFamilyName, byte[] key);

    public Map<Object, Object> getFullMapFromDB(String coulumnFamilyName);

    public RocksIterator getLastElementIteratorFromColumnFamily(String columnFamilyName);

    public Map<Object,Object> getMapAfterKeyFromDB(String columnFamilyName, Object key);
}
