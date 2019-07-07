package io.coti.historynode.services.interfaces;

import io.coti.basenode.data.interfaces.IEntity;
import io.coti.historynode.http.StoreEntitiesToStorageResponse;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface IEntityService {
    ResponseEntity<StoreEntitiesToStorageResponse> storeEntities(List<? extends IEntity> Entities);
}
