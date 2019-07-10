package io.coti.historynode.http.storageConnector.interaces;

import io.coti.basenode.http.*;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.historynode.http.StoreEntitiesToStorageResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public interface IStorageConnector<T extends GetEntitiesBulkRequest, U extends GetEntitiesBulkResponse> {
//    ResponseEntity<IResponse> getForObject(String url, Class<ResponseEntity> responseEntityClass, GetEntitiesBulkRequest getEntitiesBulkRequest);
//    ResponseEntity<IResponse> getForObject(String url, Class<ResponseEntity> responseEntityClass, GetEntityRequest getEntityRequest);
    ResponseEntity<StoreEntitiesToStorageResponse> putObject(String url, Request request);

    ResponseEntity<U> postEntitiesBulk(String url, T getEntitiesBulkRequest);
}
