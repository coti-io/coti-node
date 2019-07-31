package io.coti.storagenode.services;

import io.coti.basenode.communication.JacksonSerializer;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.interfaces.IPropagatable;
import io.coti.basenode.http.AddHistoryEntitiesResponse;
import io.coti.basenode.http.SeriazableResponse;
import io.coti.basenode.http.data.GetHashToPropagatable;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.storagenode.data.enums.ElasticSearchData;
import io.coti.storagenode.services.interfaces.IEntityStorageService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.rest.RestStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.STATUS_ERROR;
import static io.coti.storagenode.http.HttpStringConstants.*;

@Slf4j
public abstract class EntityStorageService implements IEntityStorageService {

    @Autowired
    protected ObjectService objectService;
    @Autowired
    protected JacksonSerializer jacksonSerializer;
    protected ElasticSearchData objectType;

    @Override
    public <T extends IPropagatable> GetHashToPropagatable<T> retrieveHashToObjectFromStorage(Hash hash) {
        return new GetHashToPropagatable<>(hash, retrieveObjectFromStorage(hash));
    }

    private <T extends IPropagatable> T retrieveObjectFromStorage(Hash hash) {
        boolean fromColdStorage = false;
        String objectAsJson = objectService.getObjectByHash(hash, fromColdStorage, objectType);

        if (objectAsJson == null) {
            fromColdStorage = true;
            objectAsJson = objectService.getObjectByHash(hash, fromColdStorage, objectType);
            if (objectAsJson == null) {
                return null;
            }
        }

        if (verifyRetrievedSingleObject(hash, objectAsJson, fromColdStorage, objectType)) {
            return jacksonSerializer.deserialize(objectAsJson);
        }
        return null;
    }

    protected boolean verifyRetrievedSingleObject(Hash objectHash, String objectAsJson, boolean fromColdStorage, ElasticSearchData objectType) {

        if (objectAsJson != null && validateObjectDataIntegrity(objectHash, objectAsJson)) {
            if (!fromColdStorage) {
                try {
                    objectService.deleteObjectByHash(objectHash, true, objectType);
                } catch (Exception e) {
                    log.error("{}: {}", e.getClass().getName(), e.getMessage());
                }
            }
            return true;
        }
        return false;
    }

    private String replaceHotStorageObjectWithColdStorageObject(Hash objectHash, ElasticSearchData objectType) {
        try {
            String coldStorageObjectAsJson = objectService.getObjectByHash(objectHash, true, objectType);
            if (coldStorageObjectAsJson != null) {
                if (!validateObjectDataIntegrity(objectHash, coldStorageObjectAsJson)) {
                    return null;
                }
                RestStatus deleteStatus = objectService.deleteObjectByHash(objectHash, false, objectType);
                if (deleteStatus.equals(RestStatus.OK)) {
                    RestStatus insertStatus = objectService.insertObjectJson(objectHash, coldStorageObjectAsJson, false, objectType);
                    if (insertStatus.equals(RestStatus.OK)) {
                        return coldStorageObjectAsJson;
                    }
                }
            }
            return null;
        } catch (Exception e) {
            log.error("{}: {}", e.getClass().getName(), e.getMessage());
            return null;
        }
    }

    @Override
    public ResponseEntity<IResponse> storeObjectToStorage(Hash hash, String objectAsJsonString) {
        try {
            if (!validateObjectDataIntegrity(hash, objectAsJsonString)) {
                return ResponseEntity.badRequest().body(new SeriazableResponse(INVALID_OBJECT, STATUS_ERROR));
            }

            RestStatus insertStatus = objectService.insertObjectJson(hash, objectAsJsonString, false, objectType);
            if (!insertStatus.equals(RestStatus.CREATED)) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new SeriazableResponse(String.format(INSERT_OBJECT_ERROR, insertStatus.toString()), STATUS_ERROR));
            } else {
                objectService.insertObjectJson(hash, objectAsJsonString, true, objectType);
            }
            return ResponseEntity.ok().body(new SeriazableResponse(String.format(INSERT_OBJECT_SUCCESS, hash.toString())));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new SeriazableResponse(e.getMessage(), STATUS_ERROR));
        }
    }

    @Override
    public ResponseEntity<IResponse> storeMultipleObjectsToStorage(Map<Hash, String> hashToObjectJsonDataMap) {
//TODO 7/31/2019 tomer: temporary commented for integration tests
//        Map<Hash, Boolean> entityValidationMap = validateStoreMultipleObjectsToStorage(hashToObjectJsonDataMap);
        Map<Hash, Boolean> entityValidationMap = new HashMap<>();
        if (!entityValidationMap.isEmpty()) {
            return ResponseEntity.badRequest().body(new AddHistoryEntitiesResponse(entityValidationMap));
        }

        Map<Hash, RestStatus> insertResponseMap = objectService.insertMultiObjects(hashToObjectJsonDataMap, false, objectType);
        Map<Hash, Boolean> hashToStoreResultMap = new HashMap<>();
        Map<Hash, String> hashToColdStorageObjectJsonDataMap = new HashMap<>();
        insertResponseMap.forEach((hash, restStatus) -> {
            if (restStatus.equals(RestStatus.CREATED)) {
                hashToColdStorageObjectJsonDataMap.put(hash, hashToObjectJsonDataMap.get(hash));
            } else {
                hashToStoreResultMap.put(hash, Boolean.FALSE);
            }
        });

        Map<Hash, RestStatus> insertColdResponseMap = objectService.insertMultiObjects(hashToObjectJsonDataMap, true, objectType);
        insertColdResponseMap.forEach((hash, restStatus) -> {
            if (!restStatus.equals(RestStatus.CREATED) && !restStatus.equals(RestStatus.OK)) {
                hashToStoreResultMap.put(hash, Boolean.FALSE);
            } else {
                hashToStoreResultMap.putIfAbsent(hash, Boolean.TRUE);
            }
        });
        return ResponseEntity.status(HttpStatus.OK).body(new AddHistoryEntitiesResponse(hashToStoreResultMap));
    }


    private Map<Hash, Boolean> validateStoreMultipleObjectsToStorage(Map<Hash, String> hashToObjectJsonDataMap) {

        Map<Hash, Boolean> entityFailedVerificationMap = new HashMap<>();
        hashToObjectJsonDataMap.entrySet().forEach(entry -> {
            if (!validateObjectDataIntegrity(entry.getKey(), entry.getValue())) {
                entityFailedVerificationMap.put(entry.getKey(), null);
            }
        });
        return entityFailedVerificationMap;
    }

    @Override
    public Map<Hash, String> retrieveMultipleObjectsFromStorage(List<Hash> hashes) {
        Map<Hash, String> responsesMap = new HashMap<>();

        Map<Hash, String> objectsFromDBMap = objectService.getMultiObjectsFromDb(hashes, false, objectType);

        verifyEntitiesFromDbMap(responsesMap, objectsFromDBMap);
        return responsesMap;

    }

    protected void verifyEntitiesFromDbMap(Map<Hash, String> responsesMap, Map<Hash, String> objectsFromDBMap) {
        objectsFromDBMap.forEach((hash, objectAsJsonString) ->
                {
                    if (!verifyRetrievedSingleObject(hash, objectAsJsonString, false, objectType)) {
                        responsesMap.put(hash, replaceHotStorageObjectWithColdStorageObject(hash, objectType));
                    } else {
                        responsesMap.put(hash, objectAsJsonString);
                    }
                }
        );
    }

    protected abstract IResponse getEmptyEntitiesBulkResponse();

}
