package io.coti.storagenode.services.interfaces;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.interfaces.IResponse;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface IObjectService {
    ResponseEntity<IResponse> insertObjectJson(Hash hash, String objectAsJson, boolean insertToMainStorage);

    ResponseEntity<IResponse> insertMultiObjects(Map<Hash, String> hashToObjectJsonDataMap, boolean insertToMainStorage);

    ResponseEntity<IResponse> getObjectByHash(Hash hash, boolean getFromMainStorage);

    ResponseEntity<IResponse> getMultiObjectsFromDb(List<Hash> hashes, boolean getFromMainStorage);

    ResponseEntity<IResponse> deleteMultiObjectsFromDb(List<Hash> hashes, boolean deleteFromMainStorage);

    ResponseEntity<IResponse> deleteObjectByHash(Hash hash, boolean deleteFromMainStorage);
}
