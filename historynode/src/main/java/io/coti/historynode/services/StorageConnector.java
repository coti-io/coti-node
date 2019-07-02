package io.coti.historynode.services;

import io.coti.basenode.http.GetEntitiesBulkRequest;
import io.coti.basenode.http.Request;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.historynode.services.interfaces.IStorageConnector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Slf4j
public abstract class StorageConnector implements IStorageConnector {

    public ResponseEntity<IResponse> getForObject(String url, Class<ResponseEntity> responseEntityClass, GetEntitiesBulkRequest getEntitiesBulkRequest) {

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<GetEntitiesBulkRequest> requestEntity = new HttpEntity<>(getEntitiesBulkRequest, headers);


        // TODO:  this currently fails as Body details are lost
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<List<IResponse>> exchangeResponse = restTemplate.exchange(url, HttpMethod.GET, requestEntity,
                new ParameterizedTypeReference<List<IResponse>>() {
                });

        //        return restTemplate.getForObject(url, responseEntityClass, getEntitiesBulkRequest);

        // TODO: consider changing returned value into a collection
        return (ResponseEntity<IResponse>) exchangeResponse.getBody().get(0);
    }




    public void putObject(String url, Request request) {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.put(url, request);
    }
}
