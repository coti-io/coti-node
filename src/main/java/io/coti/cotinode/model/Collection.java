package io.coti.cotinode.model;

import io.coti.cotinode.data.Hash;
import io.coti.cotinode.data.IEntity;
import io.coti.cotinode.storage.Interfaces.IDatabaseConnector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.SerializationUtils;

@Slf4j
@Service
public abstract class Collection<T extends IEntity> {

    @Autowired
    IDatabaseConnector databaseConnector;

    public void init(){
        log.info("Collection init ruuning. Class: " + getClass().getName());
    }

    public void put(IEntity entity) {
        databaseConnector.put(getClass().getName(), entity);
    }

    public T getByHash(Hash hash) {
        return (T) SerializationUtils.deserialize(
                databaseConnector.getByHash(getClass().getName(), hash));
    }

    public void delete(Hash hash){
        databaseConnector.delete(getClass().getName(), hash);
    }
}
