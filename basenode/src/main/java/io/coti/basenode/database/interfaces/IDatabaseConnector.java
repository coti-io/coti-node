package io.coti.basenode.database.interfaces;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.interfaces.IEntity;
import org.rocksdb.BackupInfo;
import org.rocksdb.RocksIterator;
import org.rocksdb.WriteBatch;
import org.rocksdb.WriteOptions;

import java.util.List;

public interface IDatabaseConnector {

    void init();

    List<BackupInfo> generateDataBaseBackup(String backupPath);

    void restoreDataBase(String backupPath);

    String getDBPath();

    boolean put(String columnFamilyName, byte[] key, byte[] value);

    boolean put(String columnFamilyName, WriteOptions writeOptions, byte[] key, byte[] value);

    boolean put(String columnFamilyName, WriteBatch writeBatch, byte[] key, byte[] value);

    boolean putBatch(WriteBatch writeBatch);

    byte[] getByKey(String columnFamilyName, byte[] key);

    void delete(String columnFamilyName, byte[] key);

    RocksIterator getIterator(String columnFamilyName);

    boolean isEmpty(String columnFamilyName);

    IEntity get(Class<?> entityClass, Hash key);

    void shutdown();

    boolean compactRange();

    List<String> getLiveFilesNames();

}
