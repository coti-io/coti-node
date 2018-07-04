package io.coti.cotinode.http;

import io.coti.cotinode.http.interfaces.IPropagationCommunication;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.ArrayList;

@Data
@Slf4j
@Service
public class RestTemplatePropagation implements IPropagationCommunication {

    RestTemplate restTemplate;

    private String requestMapping = "/propagatedTransaction";

    @PostConstruct
    private void init() {
        log.info("RestTemplatePropagation  Started");
        restTemplate = new RestTemplate();
    }

    @Override
    public void propagateTransactionToNeighbor(AddTransactionRequest request, String nodeIp) {
        String url = nodeIp + requestMapping;
        try {
           // restTemplate.put(url, request);
        } catch (RestClientException e) {
            log.error("Errors when propagating to url {} {}", url);
        }
    }

    @Override
    public void propagateTransactionFromNeighbor(GetTransactionRequest getTransactionRequest, String nodeIp) {
        String url = nodeIp + requestMapping;
        try {
           // restTemplate.postForLocation(url, getTransactionRequest);
        } catch (RestClientException e) {
            log.error("Errors when propagating from url {} {}", url);
        }
    }
}
