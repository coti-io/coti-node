package io.coti.storagenode.model;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.Response;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.storagenode.data.MultiDbInsertionStatus;
import io.coti.storagenode.data.enums.ElasticSearchData;
import io.coti.storagenode.database.DbConnectorService;
import io.coti.storagenode.http.AddEntityJsonResponse;
import io.coti.storagenode.http.GetEntitiesBulkJsonResponse;
import io.coti.storagenode.http.GetEntityJsonResponse;
import io.coti.storagenode.model.interfaces.IObjectService;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.*;
import static io.coti.storagenode.http.HttpStringConstants.STATUS_NOT_FOUND;
import static io.coti.storagenode.http.HttpStringConstants.STATUS_OK;

@Service
@Slf4j
public class ObjectService implements IObjectService {

    @Autowired
    protected DbConnectorService dbConnectorService;

    @PostConstruct
    private void init() throws IOException {
        dbConnectorService.addIndexes(true);
        dbConnectorService.addIndexes(false);
    }

    public ResponseEntity<IResponse> insertMultiObjects(Map<Hash, String> hashToObjectJsonDataMap, boolean fromColdStorage, ElasticSearchData objectType) {
        Pair<MultiDbInsertionStatus, Map<Hash, String>> insertResponse = null;
        try {
            insertResponse = dbConnectorService.insertMultiObjectsToDb(objectType.getIndex(), objectType.getObjectName(), hashToObjectJsonDataMap, fromColdStorage);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response(
                            e.getMessage(),
                            STATUS_ERROR));
        }
        return ResponseEntity
                .status(dbConnectorService.getHttpStatus(insertResponse.getKey()))
                .body(new GetEntitiesBulkJsonResponse(insertResponse.getValue()));
    }

    @Override
    public ResponseEntity<IResponse> insertObjectJson(Hash hash, String objectAsJson, boolean fromColdStorage, ElasticSearchData objectType) {
        String insertResponse = null;
        try {
            insertResponse = dbConnectorService.insertObjectToDb(hash, objectAsJson, objectType.getIndex(), objectType.getObjectName(), fromColdStorage);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response(
                            e.getMessage(),
                            STATUS_ERROR));
        }
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new AddEntityJsonResponse(
                        STATUS_SUCCESS,
                        CREATED_MESSAGE, insertResponse));
    }

    @Override
    public ResponseEntity<IResponse> getMultiObjectsFromDb(List<Hash> hashes, boolean fromColdStorage, ElasticSearchData objectType) {
        Map<Hash, String> hashToObjectFromDbMap = null;
        //TODO: Define logic.
        try {
            hashToObjectFromDbMap = dbConnectorService.getMultiObjects(hashes, objectType.getIndex(), fromColdStorage, objectType.getObjectName());
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response(
                            e.getMessage(),
                            STATUS_ERROR));
        }
        return ResponseEntity.status(HttpStatus.OK)
                .body(new GetEntitiesBulkJsonResponse(hashToObjectFromDbMap));
    }

    @Override
    public ResponseEntity<IResponse> getObjectByHash(Hash hash, boolean fromColdStorage, ElasticSearchData objectType) {
        Map<String, Object> objectAsJsonMap = null;
        String objectAsJson = null;
        try {
//            objectAsJson = dbConnectorService.getObjectFromDbByHash(hash, indexName, fromColdStorage);
            objectAsJsonMap = dbConnectorService.getObjectFromDbByHash(hash, objectType.getIndex(), fromColdStorage);
            objectAsJson = (String)objectAsJsonMap.get(objectType.getObjectName());
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response(
                            e.getMessage(),
                            STATUS_ERROR));
        }
        if (objectAsJson == null) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new Response(
                            SERVER_ERROR,
                            STATUS_ERROR));
        }

        return ResponseEntity.status(HttpStatus.OK)
                .body(new GetEntityJsonResponse(hash, objectAsJson));
    }

    @Override
    public ResponseEntity<IResponse> deleteMultiObjectsFromDb(List<Hash> hashes, boolean fromColdStorage, ElasticSearchData objectType) {
        Map<Hash, String> hashToResponseMap = new HashMap<>();
        try {
            for (Hash hash : hashes) {
                hashToResponseMap.put(hash, dbConnectorService.deleteObject(hash, objectType.getIndex(), fromColdStorage));
            }

        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new Response(
                            SERVER_ERROR,
                            STATUS_ERROR));
        }
        return ResponseEntity.status(HttpStatus.OK)
                .body(new GetEntitiesBulkJsonResponse(hashToResponseMap));
    }

    @Override
    public ResponseEntity<IResponse> deleteObjectByHash(Hash hash, boolean fromColdStorage, ElasticSearchData objectType) {
        String status = dbConnectorService.deleteObject(hash, objectType.getIndex(), fromColdStorage);
        switch (status) {
            case STATUS_OK:
                return ResponseEntity.status(HttpStatus.OK)
                        .body(new GetEntityJsonResponse(hash, status));
            case STATUS_NOT_FOUND:
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new GetEntityJsonResponse(hash, status));
            default:
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(new Response(
                                SERVER_ERROR,
                                STATUS_ERROR));

        }
    }
}
