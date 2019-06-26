package io.coti.storagenode.services.interfaces;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.interfaces.IResponse;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

@Deprecated
public interface IHistoryNodesConsensusService
{
    ResponseEntity<IResponse> validateStoreMultipleObjectsConsensus(Map<Hash, String> hashToObjectAsJsonStringMap);

    ResponseEntity<IResponse> validateRetrieveMultipleObjectsConsensus(List<Hash> hashes);
}
