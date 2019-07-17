package io.coti.historynode.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.coti.basenode.data.interfaces.IEntity;
import io.coti.basenode.http.AddEntitiesBulkRequest;
import io.coti.basenode.http.EntitiesBulkJsonResponse;
import io.coti.historynode.http.StoreEntitiesToStorageResponse;
import io.coti.historynode.services.interfaces.IEntityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public abstract class EntityService implements IEntityService {

    //TODO 7/16/2019 tomer: move code to HistoryTransactionService and remove class and interface

    protected String endpoint = null;

    @Value("${storage.server.address}")
    protected String storageServerAddress;
    protected ObjectMapper mapper;

    public ResponseEntity<EntitiesBulkJsonResponse> storeEntities(List<? extends IEntity> entities) {

        AddEntitiesBulkRequest addEntitiesBulkRequest = new AddEntitiesBulkRequest();
        entities.forEach(entity ->
                {
                    try {
                        addEntitiesBulkRequest.getHashToEntityJsonDataMap().put(entity.getHash(), mapper.writeValueAsString(entity));
                    } catch (JsonProcessingException e) {
                        log.error(e.getMessage());
                    }
                }
        );

        ResponseEntity<EntitiesBulkJsonResponse> storeEntitiesToStorageResponse = storeEntitiesByType(storageServerAddress + endpoint, addEntitiesBulkRequest);
        return storeEntitiesToStorageResponse;
    }

    protected abstract ResponseEntity<EntitiesBulkJsonResponse> storeEntitiesByType(String url, AddEntitiesBulkRequest addEntitiesBulkRequest);


}
