package io.coti.historynode.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.coti.basenode.data.interfaces.IEntity;
import io.coti.basenode.http.AddEntitiesBulkRequest;
import io.coti.historynode.http.StoreEntitiesToStorageResponse;
import io.coti.historynode.services.interfaces.IStorageConnector;
import io.coti.historynode.services.interfaces.IEntityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public abstract class EntityService implements IEntityService {

    @Autowired
    protected IStorageConnector storageConnector;

    protected String endpoint = null;

    @Value("${storage.server.address}")
    protected String storageServerAddress;
    protected ObjectMapper mapper;

    public ResponseEntity<StoreEntitiesToStorageResponse> storeEntities(List<? extends IEntity> entities) {

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
        ResponseEntity<StoreEntitiesToStorageResponse> storeEntitiesToStorageResponse = storageConnector.putObject(storageServerAddress + endpoint, addEntitiesBulkRequest);
        return storeEntitiesToStorageResponse;
    }
}
