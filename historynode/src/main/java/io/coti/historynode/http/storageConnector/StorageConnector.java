package io.coti.historynode.http.storageConnector;

import io.coti.basenode.http.Request;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.historynode.http.storageConnector.interaces.IStorageConnector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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

    public ResponseEntity<IResponse> getForObject(String url, Class<ResponseEntity> responseEntityClass, Object... uriVariables) {
        return restTemplate.getForObject(url, responseEntityClass, uriVariables);
    }

    public void putObject(String url, Request request) {
        restTemplate.put(url, request);
    }
}
