package io.coti.storagenode.services.interfaces;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.interfaces.IResponse;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface IAddressService {
    ResponseEntity<IResponse> insertAddressJson(Hash hash, String addressAsJson);

    ResponseEntity<IResponse> getAddressByHash(Hash hash);

    ResponseEntity<IResponse> getMultiAddressesFromDb(List<Hash> hashes);

    ResponseEntity<IResponse> insertMultiAddresses(Map<Hash, String> hashToObjectJsonDataMap);

    ResponseEntity<IResponse> deleteMultiAddressesFromDb(List<Hash> hashes);

    ResponseEntity<IResponse> deleteAddressByHash(Hash hash);
}
