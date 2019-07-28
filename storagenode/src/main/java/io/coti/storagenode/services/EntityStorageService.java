package io.coti.storagenode.services;

import io.coti.basenode.communication.JacksonSerializer;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.interfaces.IPropagatable;
import io.coti.basenode.http.AddHistoryEntitiesResponse;
import io.coti.basenode.http.EntitiesBulkJsonResponse;
import io.coti.basenode.http.SeriazableResponse;
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
    public <T extends IPropagatable> T retrieveObjectFromStorage(Hash hash) {
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

        Map<Hash, Boolean> entityFailedVerificationMap = validateStoreMultipleObjectsToStorage(hashToObjectJsonDataMap);
        if (!entityFailedVerificationMap.isEmpty()) {
            return convertResponseStatusToBooleanAndAddFailedHashes(entityFailedVerificationMap);
        }

        Map<Hash, RestStatus> insertResponseMap = objectService.insertMultiObjects(hashToObjectJsonDataMap, false, objectType);
        Map<Hash, String> hashToColdStorageObjectJsonDataMap = new HashMap<>();
        insertResponseMap.forEach((hash, restStatus) -> {
            if (restStatus.equals(RestStatus.CREATED)) {
                hashToColdStorageObjectJsonDataMap.put(hash, hashToObjectJsonDataMap.get(hash));
            }
        });
        if (!insertResponseMap.isEmpty()) {
            objectService.insertMultiObjects(hashToColdStorageObjectJsonDataMap, true, objectType);
        }
        return convertResponseStatusToBooleanAndAddFailedHashes(response, entityFailedVerificationHashToFalse);
    }


    private Map<Hash, Boolean> validateStoreMultipleObjectsToStorage(Map<Hash, String> hashToObjectJsonDataMap) {

        Map<Hash, Boolean> entityFailedVerificationMap = new HashMap<>();
        hashToObjectJsonDataMap.entrySet().forEach(entry -> {
            if (!validateObjectDataIntegrity(entry.getKey(), entry.getValue())) {
                entityFailedVerificationMap.put(entry.getKey(), Boolean.FALSE);
            }
        });
        return entityFailedVerificationMap;
    }

    protected boolean isResponseOK(ResponseEntity<IResponse> iResponse) {
        return iResponse != null && iResponse.getStatusCode().equals(HttpStatus.OK);
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

    protected ResponseEntity<IResponse> convertResponseStatusToBooleanAndAddFailedHashes(Map<Hash, Boolean> entityFailedConversionHashToFalse) {
        Map<Hash, String> entityResponseBodyMap = ((EntitiesBulkJsonResponse) entityResponse.getBody()).getHashToEntitiesFromDbMap();
        Map<Hash, Boolean> newResponseBody = new HashMap<>();
        entityResponseBodyMap.entrySet().forEach(entry -> {
            newResponseBody.put(entry.getKey(), convertElasticWriteStatusToBoolean(entry.getValue()));
        });
        newResponseBody.putAll(entityFailedConversionHashToFalse);
        return ResponseEntity.status(entityResponse.getStatusCode()).body(new AddHistoryEntitiesResponse(newResponseBody));
    }

    private Boolean convertElasticWriteStatusToBoolean(RestStatus writeStatus) {
        return writeStatus.equals(RestStatus.CREATED);
    }

    protected abstract IResponse getEmptyEntitiesBulkResponse();

    protected abstract IResponse getEntitiesBulkResponse(Map<Hash, String> responsesMap);


}
