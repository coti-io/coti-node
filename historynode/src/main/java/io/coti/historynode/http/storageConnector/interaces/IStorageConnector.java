package io.coti.historynode.http.storageConnector.interaces;

import io.coti.basenode.http.Request;
import io.coti.basenode.http.interfaces.IResponse;
import org.springframework.http.ResponseEntity;

public interface IStorageConnector {
    ResponseEntity<IResponse> getForObject(String url, Class<ResponseEntity> responseEntityClass, Object... uriVariables);

    void putObject(String url, Request request);
}
