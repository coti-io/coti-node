package io.coti.cotinode.database.Interfaces;

import org.rocksdb.RocksIterator;

import java.util.Map;

public interface IDatabaseConnector {

    boolean put(String columnFamilyName, byte[] key, byte[] value);

    byte[] getByKey(String columnFamilyName, byte[] key);

    void delete(String columnFamilyName, byte[] key);

    public RocksIterator getIterator(String coulumnFamilyName);
}
