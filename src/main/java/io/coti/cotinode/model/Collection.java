package io.coti.cotinode.model;

import io.coti.cotinode.data.Hash;
import io.coti.cotinode.data.interfaces.IEntity;
import io.coti.cotinode.database.Interfaces.IDatabaseConnector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.SerializationUtils;

@Slf4j
@Service
public abstract class Collection<T extends IEntity> {

    @Autowired
    private IDatabaseConnector databaseConnector;

    public void init(){
        log.info("Collection init ruuning. Class: " + getClass().getName());
    }

    public void put(IEntity entity) {
        databaseConnector.put(getClass().getName(), entity.getKey().getBytes(), SerializationUtils.serialize(entity));
    }

    public T getByHash(String hashStringInHexRepresentation){
        return getByHash(new Hash(hashStringInHexRepresentation));
    }

    public T getByHash(Hash hash) {
        byte[] bytes = databaseConnector.getByKey(getClass().getName(), hash.getBytes());
        T deserialized = (T) SerializationUtils.deserialize(bytes);
        if(deserialized instanceof IEntity) {
            deserialized.setKey(hash);
        }
        return deserialized;
    }

    public void delete(Hash hash){
        databaseConnector.delete(getClass().getName(), hash.getBytes());
    }
}
