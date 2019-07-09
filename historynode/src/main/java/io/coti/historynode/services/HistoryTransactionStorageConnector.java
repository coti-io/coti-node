package io.coti.historynode.services;

import io.coti.basenode.http.GetTransactionsResponse;
import io.coti.basenode.http.Request;
import io.coti.basenode.http.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class HistoryTransactionStorageConnector<T extends Request, U extends Response> extends StorageConnector<T,U> {

    @Override
    public ResponseEntity<U> getForObject(String url, T request) {
        return null;
    }

    @Override
    public ResponseEntity<U> postForObjects(String url, T request) {
        RestTemplate restTemplate = new RestTemplate();
        return (ResponseEntity<U>) restTemplate.postForEntity(url, request, GetTransactionsResponse.class);
    }

    @Override
    public ResponseEntity<U> getForObject(String url, T request, Class<U> responseType) {
        return null;
    }

}