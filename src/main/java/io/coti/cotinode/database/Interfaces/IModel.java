package io.coti.cotinode.database.Interfaces;

import io.coti.cotinode.data.Hash;
import io.coti.cotinode.data.interfaces.IEntity;

import java.util.function.Function;

public interface IModel {
    void put(IEntity entity);

    void find(Function<IModel, Boolean> predicate);

    void getByHash(Hash hash);
}
