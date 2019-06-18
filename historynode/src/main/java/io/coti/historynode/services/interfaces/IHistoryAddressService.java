package io.coti.historynode.services.interfaces;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.HistoryNodeConsensusResult;
import io.coti.basenode.http.interfaces.IResponse;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface IHistoryAddressService {
    ResponseEntity<IResponse> getAddresses(List<Hash> addresses, HistoryNodeConsensusResult historyNodeConsensusResult);

    ResponseEntity<IResponse> getAddressesFromHistory(List<Hash> addressesHashes);
}
