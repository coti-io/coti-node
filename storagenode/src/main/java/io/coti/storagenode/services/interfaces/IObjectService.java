package io.coti.storagenode.services.interfaces;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.storagenode.data.enums.ElasticSearchData;
import org.elasticsearch.rest.RestStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface IObjectService {

    RestStatus insertObjectJson(Hash hash, String objectAsJson, boolean fromColdStorage, ElasticSearchData objectType);

    String getObjectByHash(Hash hash, boolean fromColdStorage, ElasticSearchData objectType);

    Map<Hash, String> getMultiObjectsFromDb(List<Hash> hashes, boolean fromColdStorage, ElasticSearchData objectType);

    ResponseEntity<IResponse> insertMultiObjects(Map<Hash, String> hashToObjectJsonDataMap, boolean fromColdStorage, ElasticSearchData objectType);

    ResponseEntity<IResponse> deleteMultiObjectsFromDb(List<Hash> hashes, boolean fromColdStorage, ElasticSearchData objectType);

    RestStatus deleteObjectByHash(Hash hash, boolean fromColdStorage, ElasticSearchData objectType);
}
