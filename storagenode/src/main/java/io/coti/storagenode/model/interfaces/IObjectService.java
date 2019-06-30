package io.coti.storagenode.model.interfaces;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.storagenode.data.enums.ElasticSearchData;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface IObjectService {
    ResponseEntity<IResponse> insertObjectJson(Hash hash, String objectAsJson, boolean fromColdStorage, ElasticSearchData objectType);

    ResponseEntity<IResponse> getObjectByHash(Hash hash, boolean fromColdStorage, ElasticSearchData objectType);

    ResponseEntity<IResponse> getMultiObjectsFromDb(List<Hash> hashes, boolean fromColdStorage, ElasticSearchData objectType);

    ResponseEntity<IResponse> insertMultiObjects(Map<Hash, String> hashToObjectJsonDataMap, boolean fromColdStorage, ElasticSearchData objectType);

    ResponseEntity<IResponse> deleteMultiObjectsFromDb(List<Hash> hashes, boolean fromColdStorage, ElasticSearchData objectType);

    ResponseEntity<IResponse> deleteObjectByHash(Hash hash, boolean fromColdStorage, ElasticSearchData objectType);
}
