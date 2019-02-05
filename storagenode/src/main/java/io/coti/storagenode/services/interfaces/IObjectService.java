package io.coti.storagenode.services.interfaces;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.interfaces.IResponse;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface IObjectService {
    ResponseEntity<IResponse> insertObjectJson(Hash hash, String objectAsJson);

    ResponseEntity<IResponse> getObjectByHash(Hash hash);

    ResponseEntity<IResponse> getMultiObjectsFromDb(List<Hash> hashes);

    ResponseEntity<IResponse> insertMultiObjects(Map<Hash, String> hashToObjectJsonDataMap);

    ResponseEntity<IResponse> deleteMultiObjectsFromDb(List<Hash> hashes);

    ResponseEntity<IResponse> deleteObjectByHash(Hash hash);
}
