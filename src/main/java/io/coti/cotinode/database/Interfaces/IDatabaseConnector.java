package io.coti.cotinode.database.Interfaces;

import org.rocksdb.RocksIterator;

import java.util.Map;

public interface IDatabaseConnector {

    public boolean put(String columnFamilyName, byte[] key, byte[] value);

    public byte[] getByKey(String columnFamilyName, byte[] key);

    public void delete(String columnFamilyName, byte[] key);

    public RocksIterator getIterator(String coulumnFamilyName);
}
