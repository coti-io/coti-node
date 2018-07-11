package io.coti.dspnode.services;

import io.coti.common.data.TransactionData;
import org.hibernate.validator.constraints.URL;
import org.springframework.http.HttpEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.List;

@Service
public class TransactionPropagationService {

    List<URI> neighborDspNodeURIs;


    public void PropagateTransactionToNeighbors(TransactionData transactionData){
        for(URI dspNodeUri : neighborDspNodeURIs){
            sendTransaction(dspNodeUri, transactionData);
        }
    }

    private void sendTransaction(URI dspNodeUri, TransactionData transactionData) {
        ClientHttpRequestFactory requestFactory = getClientHttpRequestFactory();
        RestTemplate restTemplate = new RestTemplate(requestFactory);
        HttpEntity<TransactionData> request = new HttpEntity<>(transactionData);
        TransactionData data = restTemplate.postForObject(dspNodeUri, request, TransactionData.class);
    }

    private ClientHttpRequestFactory getClientHttpRequestFactory() {
        int timeout = 5000;
        HttpComponentsClientHttpRequestFactory clientHttpRequestFactory
                = new HttpComponentsClientHttpRequestFactory();
        clientHttpRequestFactory.setConnectTimeout(timeout);
        return clientHttpRequestFactory;
    }
}
