package io.coti.storagenode.services.interfaces;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.interfaces.IResponse;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface IObjectService {
    ResponseEntity<IResponse> insertObjectJson(Hash hash, String objectAsJson, boolean fromColdStorage);

    ResponseEntity<IResponse> getObjectByHash(Hash hash, boolean fromColdStorage, String fieldName);

    ResponseEntity<IResponse> getMultiObjectsFromDb(List<Hash> hashes, boolean fromColdStorage, String fieldName);

    ResponseEntity<IResponse> insertMultiObjects(Map<Hash, String> hashToObjectJsonDataMap, boolean fromColdStorage);

    ResponseEntity<IResponse> deleteMultiObjectsFromDb(List<Hash> hashes, boolean fromColdStorage);

    ResponseEntity<IResponse> deleteObjectByHash(Hash hash, boolean fromColdStorage);
}
