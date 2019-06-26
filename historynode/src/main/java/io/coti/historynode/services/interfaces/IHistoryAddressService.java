package io.coti.historynode.services.interfaces;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.interfaces.IResponse;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface IHistoryAddressService {
    ResponseEntity<IResponse> getAddresses(List<Hash> addresses);

    ResponseEntity<IResponse> getAddressesFromHistory(List<Hash> addressesHashes);
}
