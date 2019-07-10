package io.coti.historynode.services;

import io.coti.basenode.http.GetEntitiesBulkRequest;
import io.coti.basenode.http.GetEntitiesBulkResponse;
import io.coti.basenode.http.GetTransactionsBulkRequest;
import io.coti.basenode.http.GetTransactionsBulkResponse;
import io.coti.historynode.http.storageConnector.StorageConnector;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
@Service
public class TransactionStorageConnector<U extends GetEntitiesBulkRequest, T extends GetEntitiesBulkResponse> extends StorageConnector {

    private RestTemplate restTemplate;

    @PostConstruct
    public void init() {
        restTemplate = new RestTemplate();
    }

    public ResponseEntity<GetTransactionsBulkResponse> postEntitiesBulk(String url, GetTransactionsBulkRequest getEntitiesBulkRequest) {
        return restTemplate.postForEntity(url,  getEntitiesBulkRequest, GetTransactionsBulkResponse.class);
    }


}
