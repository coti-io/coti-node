package io.coti.basenode.database.Interfaces;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.interfaces.IEntity;

import java.util.function.Function;

public interface IModel {
    void put(IEntity entity);

    void find(Function<IModel, Boolean> predicate);

    void getByHash(Hash hash);
}
