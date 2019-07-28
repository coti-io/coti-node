package io.coti.storagenode.services.interfaces;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.interfaces.IPropagatable;
import io.coti.basenode.http.interfaces.IResponse;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface IEntityStorageService {
    ResponseEntity<IResponse> storeObjectToStorage(Hash hash, String objectJson);

    <T extends IPropagatable> T retrieveObjectFromStorage(Hash hash);

    ResponseEntity<IResponse> storeMultipleObjectsToStorage(Map<Hash, String> hashToObjectJsonDataMap);

    Map<Hash, String> retrieveMultipleObjectsFromStorage(List<Hash> hashes);

    boolean validateObjectDataIntegrity(Hash objectHash, String objectAsJson);

}
