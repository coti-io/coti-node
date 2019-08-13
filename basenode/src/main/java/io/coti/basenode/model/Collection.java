package io.coti.basenode.model;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.interfaces.IEntity;
import io.coti.basenode.database.interfaces.IDatabaseConnector;
import lombok.extern.slf4j.Slf4j;
import org.rocksdb.RocksIterator;
import org.rocksdb.WriteBatch;
import org.rocksdb.WriteOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.SerializationUtils;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

@Slf4j
public abstract class Collection<T extends IEntity> {

    private static final int LOCK_BYTE_ARRAY_SIZE = 2;
    @Autowired
    public IDatabaseConnector databaseConnector;
    protected String columnFamilyName = getClass().getName();
    private Map<Hash, byte[]> lockByteArrayMap;

    public void init() {
        log.info("Collection init running. Class: " + columnFamilyName);
    }

    public void put(IEntity entity) {
        databaseConnector.put(columnFamilyName, entity.getHash().getBytes(), SerializationUtils.serialize(entity));
    }

    public void put(WriteOptions writeOptions, IEntity entity) {
        databaseConnector.put(columnFamilyName, writeOptions, entity.getHash().getBytes(), SerializationUtils.serialize(entity));
    }

    public void putBatch(Map<Hash, ? extends IEntity> entities) {
        WriteBatch writeBatch = new WriteBatch();
        entities.forEach((hash, entity) ->
                databaseConnector.put(columnFamilyName, writeBatch, hash.getBytes(), SerializationUtils.serialize(entity))
        );
        databaseConnector.putBatch(writeBatch);
    }

    public void delete(IEntity entity) {
        databaseConnector.delete(columnFamilyName, entity.getHash().getBytes());
    }

    public T getByHash(String hashStringInHexRepresentation) {
        return getByHash(new Hash(hashStringInHexRepresentation));
    }

    public T getByHash(Hash hash) {
        byte[] bytes = databaseConnector.getByKey(columnFamilyName, hash.getBytes());
        T deserialized = (T) SerializationUtils.deserialize(bytes);
        if (deserialized instanceof IEntity) {
            deserialized.setHash(hash);
        }
        return deserialized;
    }

    public void forEach(Consumer<T> consumer) {
        RocksIterator iterator = databaseConnector.getIterator(columnFamilyName);
        try {
            iterator.seekToFirst();
            while (iterator.isValid()) {
                T deserialized = (T) SerializationUtils.deserialize(iterator.value());
                deserialized.setHash(new Hash(iterator.key()));
                consumer.accept(deserialized);
                iterator.next();
            }
        } finally {
            iterator.close();
        }
    }

    public void lockAndGetByHash(Hash hash, Consumer<T> consumer) {
        if (lockByteArrayMap == null) {
            throw new IllegalArgumentException(String.format("Collection %s is not lockable", columnFamilyName));
        }
        if (hash.getBytes().length < LOCK_BYTE_ARRAY_SIZE) {
            throw new IllegalArgumentException(String.format("Hash bytes should be of minimum size %s", LOCK_BYTE_ARRAY_SIZE));
        }

        byte[] lockByteArray = lockByteArrayMap.get(new Hash(Arrays.copyOfRange(hash.getBytes(), 0, LOCK_BYTE_ARRAY_SIZE)));
        if (lockByteArray == null) {
            throw new IllegalArgumentException("Hash lock object doesn't exist");
        }
        synchronized (lockByteArray) {
            T entity = getByHash(hash);
            consumer.accept(entity);
        }
    }

    public RocksIterator getIterator() {
        return databaseConnector.getIterator(columnFamilyName);
    }

    public boolean isEmpty() {
        RocksIterator iterator = databaseConnector.getIterator(columnFamilyName);
        try {
            iterator.seekToFirst();
            return !iterator.isValid();
        } finally {
            iterator.close();
        }
    }

    public void deleteByHash(Hash hash) {
        databaseConnector.delete(columnFamilyName, hash.getBytes());
    }

    public void deleteAll() {
        RocksIterator iterator = databaseConnector.getIterator(columnFamilyName);
        try {
            iterator.seekToFirst();
            while (iterator.isValid()) {
                databaseConnector.delete(columnFamilyName, iterator.key());
                iterator.next();
            }
        } finally {
            iterator.close();
        }
    }

    protected void generateLockObjects() {
        lockByteArrayMap = new LinkedHashMap<>();
        for (int i = Byte.MIN_VALUE; i <= Byte.MAX_VALUE; i++) {
            for (int j = Byte.MIN_VALUE; j <= Byte.MAX_VALUE; j++) {
                byte[] byteArray = new byte[]{(byte) i, (byte) j};
                lockByteArrayMap.put(new Hash(byteArray), byteArray);
            }
        }
    }
}

