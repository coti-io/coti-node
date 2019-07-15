package io.coti.historynode.services;

import io.coti.basenode.http.BulkRequest;
import io.coti.basenode.http.BulkResponse;
import io.coti.historynode.services.interfaces.IStorageConnector;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class StorageConnector<T extends BulkRequest, U extends BulkResponse> implements IStorageConnector<T,U> {

    @Override
    public ResponseEntity<U> retrieveFromStorage(String url, T request, Class<U> responseType) {
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.postForEntity(url, request, responseType);
    }

    @Override
    public ResponseEntity<U> storeInStorage(String url, T request, Class<U> responseType) {
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<BulkRequest> requestEntity = new HttpEntity<>(request, getHttpHeaders());
        return restTemplate.exchange(url,HttpMethod.PUT, requestEntity, responseType);
    }

    private HttpHeaders getHttpHeaders(){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
