package io.coti.storagenode.services.interfaces;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.interfaces.IResponse;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface IAddressService {
    ResponseEntity<IResponse> insertAddressJson(Hash hash, String addressAsJson) throws IOException;

    ResponseEntity<IResponse> getAddressByHash(Hash hash) throws IOException;

    ResponseEntity<IResponse> getMultiAddressesFromDb(List<Hash> hashes) throws IOException;

    ResponseEntity<IResponse> insertMultiAddresses(Map<Hash, String> hashToObjectJsonDataMap);

    ResponseEntity<IResponse> deleteMultiAddressesFromDb(List<Hash> hashes);

    ResponseEntity<IResponse> deleteAddressByHash(Hash hash);
}
