package io.coti.historynode.services.interfaces;

import io.coti.basenode.data.interfaces.IEntity;

import java.util.List;

public interface IEntityService {
    void storeEntities(List<? extends IEntity> Entities);
}
