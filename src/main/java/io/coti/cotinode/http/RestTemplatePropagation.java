package io.coti.cotinode.http;

import io.coti.cotinode.data.Hash;
import io.coti.cotinode.data.TransactionData;
import io.coti.cotinode.http.interfaces.IPropagationSender;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.ArrayList;

@Data
@Slf4j
@Service
public class RestTemplatePropagation implements IPropagationSender {

    RestTemplate restTemplate;

    private String requestTransactionMapping = "/propagatedTransaction";

    @PostConstruct
    private void init() {
        log.info("RestTemplatePropagation  Started");
        restTemplate = new RestTemplate();
    }

    @Override
    public void propagateTransactionToNeighbor(TransactionData transactionData, String nodeIp) {
        String url = nodeIp + requestTransactionMapping;
        try {
           // restTemplate.put(url, request);
        } catch (RestClientException e) {
            log.error("Errors when propagating to url {} {}", url);
        }
    }

    @Override
    public ResponseEntity<Response> propagateTransactionFromNeighbor(Hash transactionHash, String nodeIp) {
        String url = nodeIp + requestTransactionMapping;
        ResponseEntity<Response> response = null;
        try {
          // response = restTemplate.postForLocation(url, getTransactionRequest);
        } catch (RestClientException e) {
            log.error("Errors when propagating from url {} {}", url);
        }
        return response;
    }

    @Override
    public GetTransactionsResponse propagateMultiTransactionFromNeighbor(GetTransactionsRequest getTransactionsRequest, String nodeIp) {
        GetTransactionsResponse getTransactionsResponse = null;
        String url = nodeIp + requestTransactionMapping;
        try {

            getTransactionsResponse = restTemplate.postForObject(url, getTransactionsRequest, GetTransactionsResponse.class);
        } catch (RestClientException e) {
            log.error("Errors when propagating from url {} {}", url);
        }
        return getTransactionsResponse;
    }
}
