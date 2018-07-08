package io.coti.common.database.Interfaces;

import org.rocksdb.RocksIterator;

public interface IDatabaseConnector {

    boolean put(String columnFamilyName, byte[] key, byte[] value);

    byte[] getByKey(String columnFamilyName, byte[] key);

    void delete(String columnFamilyName, byte[] key);

    RocksIterator getIterator(String coulumnFamilyName);

    boolean isEmpty(String columnFamilyName);
}
