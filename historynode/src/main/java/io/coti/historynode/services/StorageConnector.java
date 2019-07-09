package io.coti.historynode.services;

import io.coti.basenode.http.*;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.historynode.services.interfaces.IStorageConnector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Slf4j
public abstract class StorageConnector<T extends Request, U extends Response> implements IStorageConnector<T,U> {

    public ResponseEntity<IResponse> getForObject(String url, GetEntitiesBulkRequest getEntitiesBulkRequest, Class<ResponseEntity> responseEntityClass) {

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

    @Override
    public ResponseEntity<U> postForObjects(String url, T request) {
        RestTemplate restTemplate = new RestTemplate();
        return (ResponseEntity<U>) restTemplate.postForEntity(url, request, Response.class);
    }

    @Override
    public ResponseEntity<U> postForObjects(String url, T request, Class<U> responseType) {
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.postForEntity(url, request, responseType);
    }


    public void putObject(String url, Request request) {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.put(url, request);
    }
}
