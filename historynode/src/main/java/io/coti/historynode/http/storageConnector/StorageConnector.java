package io.coti.historynode.http.storageConnector;

import io.coti.basenode.http.*;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.historynode.http.StoreEntitiesToStorageResponse;
import io.coti.historynode.http.storageConnector.interaces.IStorageConnector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;

@Slf4j
@Service
public class StorageConnector implements IStorageConnector {
    private RestTemplate restTemplate;

    @PostConstruct
    public void init() {
        restTemplate = new RestTemplate();
    }

//public ResponseEntity<IResponse> getForObject(String url, Class<ResponseEntity> responseEntityClass, GetEntitiesBulkRequest getEntitiesBulkRequest) {
//
//    HttpHeaders headers = new HttpHeaders();
//    headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
//    HttpEntity<GetEntitiesBulkRequest> requestEntity = new HttpEntity<>(getEntitiesBulkRequest, headers);
//
//
//    // TODO:  this currently fails as Body details are lost
//    ResponseEntity<List<IResponse>> exchangeResponse = restTemplate.exchange(url, HttpMethod.GET, requestEntity,
//            new ParameterizedTypeReference<List<IResponse>>() {
//            });
//
////        return restTemplate.getForObject(url, responseEntityClass, getEntitiesBulkRequest);
//
//    // TODO: consider changing returned value into a collection
//    return (ResponseEntity<IResponse>) exchangeResponse.getBody().get(0);
//    }

//    @Override
    public ResponseEntity<IResponse> getForObject(String url, Class<ResponseEntity> responseEntityClass, GetEntityRequest getEntityRequest) {
        //TODO 6/27/2019 astolia: Implement
        return null;
    }

    @Override
    public ResponseEntity<StoreEntitiesToStorageResponse> putObject(String url, Request request) {
        //TODO 7/7/2019 tomer: Check change from put to post, move response to base node,match it with actual response from storage node
        ResponseEntity<StoreEntitiesToStorageResponse> storeEntitiesToStorageResponse = restTemplate.postForEntity(url, request, StoreEntitiesToStorageResponse.class);
        return storeEntitiesToStorageResponse;
    }

    @Override
    public ResponseEntity<GetEntitiesBulkResponse> postEntitiesBulk(String url, GetEntitiesBulkRequest getEntitiesBulkRequest) {
        return restTemplate.postForEntity(url,  getEntitiesBulkRequest,   GetEntitiesBulkResponse.class);
    }
}
