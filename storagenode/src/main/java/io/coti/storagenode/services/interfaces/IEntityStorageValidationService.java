package io.coti.storagenode.services.interfaces;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.storagenode.services.ObjectService;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface IEntityStorageValidationService
{
    ResponseEntity<IResponse> storeObjectToStorage(Hash hash, String objectJson);

    ResponseEntity<IResponse> retrieveObjectFromStorage(Hash hash, String fieldName);

    ResponseEntity<IResponse> storeMultipleObjectsToStorage(Map<Hash, String> hashToObjectJsonDataMap);

    ResponseEntity<IResponse> retrieveMultipleObjectsFromStorage(List<Hash> hashes, String fieldName);

    boolean isObjectDIOK(Hash objectHash, String objectAsJson);

    ObjectService getObjectService();
}
