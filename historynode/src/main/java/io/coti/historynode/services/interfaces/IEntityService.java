package io.coti.historynode.services.interfaces;

import io.coti.basenode.data.interfaces.IEntity;
import io.coti.basenode.http.EntitiesBulkJsonResponse;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface IEntityService {
    ResponseEntity<EntitiesBulkJsonResponse> storeEntities(List<? extends IEntity> Entities);
}
