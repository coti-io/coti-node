package io.coti.storagenode.services;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.EntitiesBulkJsonResponse;
import io.coti.basenode.http.Response;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.storagenode.data.MultiDbInsertionStatus;
import io.coti.storagenode.data.enums.ElasticSearchData;
import io.coti.storagenode.database.DbConnectorService;
import io.coti.storagenode.services.interfaces.IObjectService;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.rest.RestStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.SERVER_ERROR;
import static io.coti.basenode.http.BaseNodeHttpStringConstants.STATUS_ERROR;

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
        Pair<MultiDbInsertionStatus, Map<Hash, String>> insertResponse;
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
                .body(new EntitiesBulkJsonResponse(insertResponse.getValue()));
    }

    @Override
    public RestStatus insertObjectJson(Hash hash, String objectAsJson, boolean fromColdStorage, ElasticSearchData objectType) {
        return dbConnectorService.insertObjectToDb(hash, objectAsJson, objectType.getIndex(), objectType.getObjectName(), fromColdStorage).status();
    }

    @Override
    public Map<Hash, String> getMultiObjectsFromDb(List<Hash> hashes, boolean fromColdStorage, ElasticSearchData objectType) {
        return dbConnectorService.getMultiObjects(hashes, objectType.getIndex(), fromColdStorage, objectType.getObjectName());
    }

    @Override
    public String getObjectByHash(Hash hash, boolean fromColdStorage, ElasticSearchData objectType) {
        GetResponse getResponse = dbConnectorService.getObjectFromDbByHash(hash, objectType.getIndex(), fromColdStorage);
        return getResponse.isExists() ? (String) getResponse.getSourceAsMap().get(objectType.getObjectName()) : null;

    }

    @Override
    public ResponseEntity<IResponse> deleteMultiObjectsFromDb(List<Hash> hashes, boolean fromColdStorage, ElasticSearchData objectType) {
        Map<Hash, String> hashToResponseMap = new HashMap<>();
        try {
            for (Hash hash : hashes) {

                try {
                    hashToResponseMap.put(hash, dbConnectorService.deleteObject(hash, objectType.getIndex(), fromColdStorage));
                } catch (Exception e) {

                }
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
                .body(new EntitiesBulkJsonResponse(hashToResponseMap));
    }

    @Override
    public RestStatus deleteObjectByHash(Hash hash, boolean fromColdStorage, ElasticSearchData objectType) {
        return dbConnectorService.deleteObject(hash, objectType.getIndex(), fromColdStorage).status();

    }
}
