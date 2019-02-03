package io.coti.storagenode.services.interfaces;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.storagenode.data.ObjectDocument;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface IAddressService {
    ResponseEntity<IResponse> insertAddressJson(Hash hash, String addressAsJson) throws IOException;

    ResponseEntity<IResponse> getAddressByHash(Hash hash) throws IOException;

    ResponseEntity<IResponse> getMultiAddressesFromDb(Map<Hash, String> hashAndIndexNameMap) throws IOException;

    ResponseEntity<IResponse> insertMultiAddressesToDb(List<ObjectDocument> objectDocumentList) throws IOException;
}
