package io.coti.basenode.model;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.interfaces.IEntity;
import io.coti.basenode.database.Interfaces.IDatabaseConnector;
import lombok.extern.slf4j.Slf4j;
import org.rocksdb.RocksIterator;
import org.rocksdb.WriteBatch;
import org.rocksdb.WriteOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.SerializationUtils;

import java.util.Map;
import java.util.function.Consumer;

@Slf4j
public abstract class Collection<T extends IEntity> {

    @Autowired
    public IDatabaseConnector databaseConnector;
    protected String columnFamilyName = getClass().getName();

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
        iterator.seekToFirst();
        while (iterator.isValid()) {
            T deserialized = (T) SerializationUtils.deserialize(iterator.value());
            deserialized.setHash(new Hash(iterator.key()));
            consumer.accept(deserialized);
            iterator.next();
        }
    }

    public boolean isEmpty() {
        RocksIterator iterator = databaseConnector.getIterator(columnFamilyName);
        iterator.seekToFirst();
        return !iterator.isValid();
    }
}

