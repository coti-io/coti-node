package io.coti.cotinode.storage.Interfaces;

import io.coti.cotinode.data.Hash;
import io.coti.cotinode.data.IEntity;
import io.coti.cotinode.data.TransactionData;
import io.coti.cotinode.model.*;

import java.util.List;

public interface IDatabaseConnector {
    void init();

    boolean put(IEntity IEntity);

    List<IEntity> getAll(String dataObjectClassName);

    void shutdown();

    byte[] getByHash(String columnFamilyName, Hash hash);

    void delete(String dataObjectClassName, Hash hash);
}
