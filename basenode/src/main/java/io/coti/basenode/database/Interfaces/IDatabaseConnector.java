package io.coti.basenode.database.Interfaces;

import org.rocksdb.RocksIterator;
import org.rocksdb.WriteBatch;
import org.rocksdb.WriteOptions;

public interface IDatabaseConnector {

    boolean put(String columnFamilyName, byte[] key, byte[] value);

    boolean put(String columnFamilyName, WriteOptions writeOptions, byte[] key, byte[] value);

    boolean put(String columnFamilyName, WriteBatch writeBatch, byte[] key, byte[] value);

    boolean putBatch(WriteBatch writeBatch);

    byte[] getByKey(String columnFamilyName, byte[] key);

    void delete(String columnFamilyName, byte[] key);

    RocksIterator getIterator(String columnFamilyName);

    boolean isEmpty(String columnFamilyName);

    void init();

    void shutdown();
}
