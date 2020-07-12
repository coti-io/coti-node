package io.coti.basenode.model;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.interfaces.IEntity;
import io.coti.basenode.database.interfaces.IDatabaseConnector;
import io.coti.basenode.exceptions.DataBaseDeleteException;
import io.coti.basenode.exceptions.DataBaseWriteException;
import lombok.extern.slf4j.Slf4j;
import org.rocksdb.RocksIterator;
import org.rocksdb.WriteBatch;
import org.rocksdb.WriteOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.SerializationUtils;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
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
        if (entity == null) {
            throw new DataBaseWriteException("Null entity to write to database");
        }
        databaseConnector.put(columnFamilyName, entity.getHash().getBytes(), SerializationUtils.serialize(entity));
    }

    public void put(WriteOptions writeOptions, IEntity entity) {
        if (entity == null) {
            throw new DataBaseWriteException("Null entity to write to database");
        }
        databaseConnector.put(columnFamilyName, writeOptions, entity.getHash().getBytes(), SerializationUtils.serialize(entity));
    }

    public void putBatch(Map<Hash, ? extends IEntity> entities) {
        WriteBatch writeBatch = new WriteBatch();
        entities.forEach((hash, entity) -> {
                    if (entity == null) {
                        throw new DataBaseWriteException("Null entity to write from database");
                    }
                    databaseConnector.put(columnFamilyName, writeBatch, hash.getBytes(), SerializationUtils.serialize(entity));

                }
        );
        databaseConnector.putBatch(writeBatch);
    }

    public void delete(IEntity entity) {
        if (entity == null) {
            throw new DataBaseDeleteException("Null entity to delete from database");
        }
        databaseConnector.delete(columnFamilyName, entity.getHash().getBytes());
    }

    public T getByHash(String hashStringInHexRepresentation) {
        return getByHash(new Hash(hashStringInHexRepresentation));
    }

    public T getByHash(Hash hash) {
        try {
            byte[] bytes = databaseConnector.getByKey(columnFamilyName, hash.getBytes());
            if (bytes == null || bytes.length == 0) {
                return null;
            }
            return getDeserializedValue(hash, bytes);
        } catch (Exception e) {
            log.error("Error at getting by hash from column family {}", columnFamilyName, e);
            return null;
        }
    }

    private T getDeserializedValue(Hash hash, byte[] serializedValue) {
        T deserialized = (T) SerializationUtils.deserialize(serializedValue);
        if (deserialized != null) {
            deserialized.setHash(hash);
        }
        return deserialized;
    }

    public void forEach(Consumer<T> consumer) {
        try (RocksIterator iterator = getIterator()) {
            iterator.seekToFirst();
            while (iterator.isValid()) {
                T deserialized = getDeserializedValue(iterator);
                consumer.accept(deserialized);
                iterator.next();
            }
        }
    }

    public void forEach(BiConsumer<byte[], byte[]> biConsumer) {
        try (RocksIterator iterator = getIterator()) {
            iterator.seekToFirst();
            while (iterator.isValid()) {
                biConsumer.accept(iterator.key(), iterator.value());
                iterator.next();
            }
        }
    }

    public void forEachWithLastIteration(BiConsumer<T, Boolean> biConsumer) {
        try (RocksIterator iterator = getIterator()) {
            iterator.seekToFirst();
            while (iterator.isValid()) {
                T deserialized = getDeserializedValue(iterator);
                iterator.next();
                boolean isLastIteration = !iterator.isValid();
                biConsumer.accept(deserialized, isLastIteration);
            }
        }
    }

    private T getDeserializedValue(RocksIterator iterator) {
        return getDeserializedValue(new Hash(iterator.key()), iterator.value());
    }

    public void lockAndGetByHash(Hash hash, Consumer<T> consumer) {
        if (lockByteArrayMap == null) {
            throw new IllegalArgumentException(String.format("Collection %s is not lockable", columnFamilyName));
        }
        if (hash.getBytes().length < LOCK_BYTE_ARRAY_SIZE) {
            throw new IllegalArgumentException(String.format("Hash bytes should be of minimum size %s", LOCK_BYTE_ARRAY_SIZE));
        }

        final byte[] lockByteArray = lockByteArrayMap.get(new Hash(Arrays.copyOfRange(hash.getBytes(), 0, LOCK_BYTE_ARRAY_SIZE)));
        if (lockByteArray == null) {
            throw new IllegalArgumentException("Hash lock object doesn't exist");
        }
        synchronized (lockByteArray) {
            T entity = getByHash(hash);
            consumer.accept(entity);
        }
    }

    private RocksIterator getIterator() {
        return databaseConnector.getIterator(columnFamilyName);
    }

    public boolean isEmpty() {
        try (RocksIterator iterator = databaseConnector.getIterator(columnFamilyName)) {
            iterator.seekToFirst();
            return !iterator.isValid();
        }
    }

    public void deleteByHash(Hash hash) {
        databaseConnector.delete(columnFamilyName, hash.getBytes());
    }

    public void deleteAll() {
        try (RocksIterator iterator = databaseConnector.getIterator(columnFamilyName)) {
            iterator.seekToFirst();
            while (iterator.isValid()) {
                databaseConnector.delete(columnFamilyName, iterator.key());
                iterator.next();
            }
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

