package io.coti.storagenode.services.interfaces;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.HistoryNodeConsensusResult;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.storagenode.services.ObjectService;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface IEntityStorageValidationService
{
    ResponseEntity<IResponse> storeObjectToStorage(Hash hash, String objectJson, HistoryNodeConsensusResult historyNodeConsensusResult);

    ResponseEntity<IResponse> retrieveObjectFromStorage(Hash hash, HistoryNodeConsensusResult historyNodeConsensusResult);

    ResponseEntity<IResponse> storeMultipleObjectsToStorage(Map<Hash, String> hashToObjectJsonDataMap, HistoryNodeConsensusResult historyNodeConsensusResult);

    Map<Hash, ResponseEntity<IResponse>> retrieveMultipleObjectsFromStorage(List<Hash> hashes, HistoryNodeConsensusResult historyNodeConsensusResult);

    boolean isObjectDIOK(Hash objectHash, String objectAsJson);

    ObjectService getObjectService();
}
