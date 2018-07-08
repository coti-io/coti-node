package io.coti.common.database.Interfaces;

import io.coti.common.data.Hash;
import io.coti.common.data.interfaces.IEntity;

import java.util.function.Function;

public interface IModel {
    void put(IEntity entity);

    void find(Function<IModel, Boolean> predicate);

    void getByHash(Hash hash);
}
