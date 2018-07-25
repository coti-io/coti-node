package io.coti.common.model;

import io.coti.common.data.Hash;
import io.coti.common.data.interfaces.IEntity;
import io.coti.common.database.Interfaces.IDatabaseConnector;
import lombok.extern.slf4j.Slf4j;
import org.rocksdb.RocksIterator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.SerializationUtils;

@Slf4j
public abstract class Collection<T extends IEntity> {

    protected String columnFamilyName = getClass().getName();

    @Autowired
    public IDatabaseConnector databaseConnector;

    public void init(){
        log.info("Collection init running. Class: " + columnFamilyName);
    }

    public void put(IEntity entity) {
        databaseConnector.put(columnFamilyName, entity.getHash().getBytes(), SerializationUtils.serialize(entity));
    }

    public T getByHash(String hashStringInHexRepresentation){
        return getByHash(new Hash(hashStringInHexRepresentation));
    }

    public T getByHash(Hash hash) {
        byte[] bytes = databaseConnector.getByKey(columnFamilyName, hash.getBytes());
        T deserialized = (T) SerializationUtils.deserialize(bytes);
        if(deserialized instanceof IEntity) {
            deserialized.setHash(hash);
        }
        return deserialized;
    }


    public DbItem<T> getByHashItem(Hash hash) {

        T deserialized = getByHash(hash);
        return new DbItem(deserialized);
    }

    public void delete(Hash hash){
        databaseConnector.delete(columnFamilyName, hash.getBytes());
    }

    public boolean isEmpty(){
        return databaseConnector.isEmpty(columnFamilyName);
    }

    public RocksIterator getIterator(){
        return databaseConnector.getIterator(columnFamilyName);
    }
}

