package io.coti.historynode.services.interfaces;

import io.coti.basenode.http.BulkRequest;
import io.coti.basenode.http.BulkResponse;
import org.springframework.http.ResponseEntity;

public interface IStorageConnector<T extends BulkRequest, U extends BulkResponse> {

    ResponseEntity<U> retrieveFromStorage(String url, T request, Class<U> responseType);
    ResponseEntity<U> storeInStorage(String url, T request, Class<U> responseType);


}
