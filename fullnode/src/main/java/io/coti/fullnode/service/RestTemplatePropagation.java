package io.coti.fullnode.service;

import io.coti.common.data.Hash;
import io.coti.common.data.TransactionData;
import io.coti.common.http.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

@Data
@Slf4j
@Service
public class RestTemplatePropagation implements IPropagationSender {

    @Value("${nodes.file}")
    private String dspNodesFile;

    private RestTemplate restTemplate;
    private String requestTransactionMapping = "/propagatedTransaction";
    private List<String> dspNodesList;
    private String mainDspNodeIp;
    private String secondDspNodeIp;

    @PostConstruct
    private void init() {
        log.info("RestTemplatePropagation  Started");
        restTemplate = new RestTemplate();
        dspNodesList = new ArrayList<>();
        initDspNodes();
    }

    @Override
    public void propagateTransactionToDspNode(TransactionData transactionData) {
        propagateTransactionToSpecificDspNode(transactionData, mainDspNodeIp);
        propagateTransactionToSpecificDspNode(transactionData, secondDspNodeIp);
    }

    public void propagateTransactionToSpecificDspNode(TransactionData transactionData, String node) {
        String url = node + "propagateTransactionToDsp";
        AddTransactionDataRequest request = new AddTransactionDataRequest();
        request.transactionData = transactionData;
        try {
            // restTemplate.put(url, request);
        } catch (RestClientException e) {
            log.error("Errors when propagating to url {} {}", url);
        }
    }

    @Override
    public TransactionData propagateTransactionFromDspByHash(Hash transactionHash) {
        String url = mainDspNodeIp + "propagateTransactionFromDspByHash";
        ResponseEntity<GetTransactionResponse> response = null;
        GetTransactionDataRequest request = new GetTransactionDataRequest();
        request.transactionHash = transactionHash;

        try {
            response = restTemplate.postForObject(url, request, ResponseEntity.class);
        } catch (RestClientException e) {
            log.error("Errors when propagating from url {} {}", url);
        }
        return response.getBody().getTransactionData();
    }

    @Override
    public TransactionData propagateTransactionFromDspByIndex(int index) {
        return getTransactionByIndex(index, mainDspNodeIp);
    }

    @Override
    public List<TransactionData> propagateMultiTransactionFromDsp(int lastIndex) {
        ResponseEntity<GetTransactionsResponse> getTransactionsResponse = null;
        String url = mainDspNodeIp + "propagateMultiTransactionFromDsp";
        GetTransactionsDataRequest getTransactionsRequest = new GetTransactionsDataRequest();
        getTransactionsRequest.lastIndex = lastIndex;
        try {

            getTransactionsResponse = restTemplate.postForObject(url, getTransactionsRequest, ResponseEntity.class);
        } catch (RestClientException e) {
            log.error("Errors when propagating from url {} {}", url);
        }
        return getTransactionsResponse.getBody().getTransactionsData();
    }


    @Override
    public Hash getLastTransactionHashFromOtherDsp(int index) {
        return getTransactionByIndex(index, secondDspNodeIp).getHash();
    }


    private TransactionData getTransactionByIndex(int index, String dspNodeIp) {
        String url = dspNodeIp + "propagateTransactionFromDspByHash";
        ResponseEntity<GetTransactionResponse> response = null;
        GetTransactionDataRequest request = new GetTransactionDataRequest();
        request.index = index;
        try {
            response = restTemplate.postForObject(url, request, ResponseEntity.class);
        } catch (RestClientException e) {
            log.error("Errors when propagating from url {} {}", url);
        }
        return response.getBody().getTransactionData();
    }

    public void initDspNodes() {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(dspNodesFile).getFile());
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                dspNodesList.add(scanner.nextLine().trim());
            }
        } catch (Exception ex) {
            log.error("An error while loading the nodesList", ex);
        }
        Random random = new Random();
        mainDspNodeIp = dspNodesList.get(random.nextInt(dspNodesList.size()));
        dspNodesList.remove(mainDspNodeIp);
        secondDspNodeIp = dspNodesList.get(random.nextInt(dspNodesList.size()));

    }
}
