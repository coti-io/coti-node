package io.coti.storagenode.services;

import io.coti.basenode.data.Hash;
import io.coti.storagenode.data.enums.ElasticSearchData;
import io.coti.storagenode.services.interfaces.IObjectService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.rest.RestStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.coti.storagenode.services.NodeServiceManager.dbConnectorService;

@Service
@Slf4j
public class ObjectService implements IObjectService {

    @Override
    public void init() throws IOException {
        dbConnectorService.addIndexes(true);
        dbConnectorService.addIndexes(false);
    }

    @Override
    public Map<Hash, RestStatus> insertMultiObjects(Map<Hash, String> hashToObjectJsonDataMap, boolean fromColdStorage, ElasticSearchData objectType) {
        Map<Hash, RestStatus> hashToRestStatusMap = new HashMap<>();
        BulkResponse insertResponse = dbConnectorService.insertMultiObjectsToDb(objectType.getIndex(), objectType.getObjectName(), hashToObjectJsonDataMap, fromColdStorage);
        Arrays.asList(insertResponse.getItems()).forEach(bulkItemResponse -> hashToRestStatusMap.put(new Hash(bulkItemResponse.getId()), bulkItemResponse.status()));
        return hashToRestStatusMap;
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
    public Map<Hash, RestStatus> deleteMultiObjectsFromDb(List<Hash> hashes, boolean fromColdStorage, ElasticSearchData objectType) {
        Map<Hash, RestStatus> hashToResponseMap = new HashMap<>();

        hashes.forEach(hash -> hashToResponseMap.put(hash, this.deleteObjectByHash(hash, fromColdStorage, objectType)));
        return hashToResponseMap;
    }

    @Override
    public RestStatus deleteObjectByHash(Hash hash, boolean fromColdStorage, ElasticSearchData objectType) {
        return dbConnectorService.deleteObject(hash, objectType.getIndex(), fromColdStorage).status();

    }
}
