package io.coti.cotinode.model;

import io.coti.cotinode.data.Hash;
import io.coti.cotinode.data.IEntity;
import io.coti.cotinode.storage.Interfaces.IDatabaseConnector;
import io.coti.cotinode.storage.RocksDBConnector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.SerializationUtils;

import java.util.List;

@Slf4j
public abstract class Collection<T extends IEntity> {

    public Class<T> dataObjectClass;

    @Autowired
    IDatabaseConnector databaseConnector;

    public void init(){
        databaseConnector = new RocksDBConnector();
    }

    public void put(IEntity entity) {
        databaseConnector.put(entity);
    }

    public T getByHash(Hash hash) {
        return (T) SerializationUtils.deserialize(
                databaseConnector.getByHash(dataObjectClass.getName(), hash));
    }

    public List<T> getAll(){
        return (List<T>)databaseConnector.getAll(dataObjectClass.getName());
    }

    public void delete(Hash hash){
        databaseConnector.delete(dataObjectClass.getName(), hash);
    }
}
