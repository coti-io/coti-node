package io.coti.common.model;

import io.coti.common.data.interfaces.IEntity;

public class DbItem<T extends IEntity>
{

    public boolean isExists;
    public T item;

    public DbItem(T dbObject){
        this.item = dbObject;
        isExists = dbObject != null;
    }
}
