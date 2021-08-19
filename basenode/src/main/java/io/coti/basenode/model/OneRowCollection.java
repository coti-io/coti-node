package io.coti.basenode.model;

import io.coti.basenode.data.interfaces.IEntity;
import org.rocksdb.RocksIterator;
import org.springframework.util.SerializationUtils;

public abstract class OneRowCollection<T extends IEntity> extends Collection<T> {

    @Override
    public void put(IEntity entity) {
        RocksIterator iterator = databaseConnector.getIterator(columnFamilyName);
        try {
            iterator.seekToFirst();
            if (iterator.isValid()) {
                IEntity previous = (IEntity) SerializationUtils.deserialize(iterator.value());
                delete(previous);
            }
            super.put(entity);
        } finally {
            iterator.close();
        }
    }

    public T get() {
        T entity = null;
        RocksIterator iterator = databaseConnector.getIterator(columnFamilyName);
        try {
            iterator.seekToFirst();
            if (iterator.isValid()) {
                entity = (T) SerializationUtils.deserialize(iterator.value());
            }
            return entity;
        } finally {
            iterator.close();
        }
    }
}
