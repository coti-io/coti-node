package io.coti.historynode.http.storageConnector.interaces;

import io.coti.basenode.http.GetEntitiesBulkRequest;
import io.coti.basenode.http.GetEntityRequest;
import io.coti.basenode.http.Request;
import io.coti.basenode.http.interfaces.IResponse;
import org.springframework.http.ResponseEntity;

public interface IStorageConnector {
    ResponseEntity<IResponse> getForObject(String url, Class<ResponseEntity> responseEntityClass, GetEntitiesBulkRequest getEntitiesBulkRequest);
    ResponseEntity<IResponse> getForObject(String url, Class<ResponseEntity> responseEntityClass, GetEntityRequest getEntityRequest);
    void putObject(String url, Request request);
}
