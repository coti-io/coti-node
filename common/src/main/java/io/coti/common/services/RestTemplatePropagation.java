package io.coti.common.services;

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
    private String firstDspNodeIp;
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
        propagateTransactionToSpecificDspNode(transactionData, firstDspNodeIp);
        propagateTransactionToSpecificDspNode(transactionData, secondDspNodeIp);
    }

    @Override
    public void propagateTransactionToSpecificDspNode(TransactionData transactionData, String node) {
        String url = node + "/propagateTransactionToDsp";
        AddTransactionDataRequest request = new AddTransactionDataRequest();
        request.transactionData = transactionData;
        try {
            //restTemplate.put(url, request);
        } catch (RestClientException e) {
            log.error("Errors when propagating to url {} {}", url);
        }
    }

    @Override
    public String getMostUpdatedDspNode() {
        if (getLastIndexFromDspNode(firstDspNodeIp) >= getLastIndexFromDspNode(secondDspNodeIp)) {
            return firstDspNodeIp;
        }
        return secondDspNodeIp;
    }

    private int getLastIndexFromDspNode(String dspNodeUrl) {
        String url = dspNodeUrl + "/getLastIndex";
        ResponseEntity<GetLastIndexResponse> response = null;
        try {
            //response = restTemplate.postForObject(url, null, ResponseEntity.class);
        } catch (RestClientException e) {
            log.error("Errors when propagating from url {} {}", url);
        }
        return response.getBody().getLastIndex();
    }


    @Override
    public TransactionData propagateTransactionFromDspByHash(Hash transactionHash) {
        TransactionData transactionData = getTransactionByHash(transactionHash, firstDspNodeIp);
        if (transactionData != null) {
            return transactionData;
        }
        return getTransactionByHash(transactionHash, secondDspNodeIp);
    }

    @Override
    public TransactionData propagateTransactionFromDspByIndex(int index) {
        TransactionData transactionData = getTransactionByIndex(index, firstDspNodeIp);
        if (transactionData != null) {
            return transactionData;
        }
        return getTransactionByIndex(index, secondDspNodeIp);
    }

    @Override
    public List<TransactionData> propagateMultiTransactionFromDsp(int lastIndex) {
        ResponseEntity<GetTransactionsResponse> getTransactionsResponse = null;
        String url = getMostUpdatedDspNode() + "/propagateMultiTransactionFromDsp";
        GetTransactionsDataRequest getTransactionsRequest = new GetTransactionsDataRequest();
        getTransactionsRequest.lastIndex = lastIndex;
        try {

            //   getTransactionsResponse = restTemplate.postForObject(url, getTransactionsRequest, ResponseEntity.class);
        } catch (RestClientException e) {
            log.error("Errors when propagating from url {} {}", url);
        }
        return getTransactionsResponse.getBody().getTransactionsData();
    }

    private TransactionData getTransactionByIndex(int index, String dspNodeIp) {
        String url = dspNodeIp + "/propagateTransactionFromDspByIndex";
        ResponseEntity<GetTransactionResponse> response = null;
        GetTransactionDataRequest request = new GetTransactionDataRequest();
        request.index = index;
        try {
            //response = restTemplate.postForObject(url, request, ResponseEntity.class);
        } catch (RestClientException e) {
            log.error("Errors when propagating from url {} {}", url);
        }
        return response.getBody().getTransactionData();
    }

    private TransactionData getTransactionByHash(Hash transactionHash, String dspNodeIp) {
        String url = dspNodeIp + "/propagateTransactionFromDspByHash";
        GetTransactionDataRequest request = new GetTransactionDataRequest();
        ResponseEntity<GetTransactionResponse> response = null;
        request.transactionHash = transactionHash;

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
        firstDspNodeIp = dspNodesList.get(random.nextInt(dspNodesList.size()));
        log.info("first dsp node: {}", firstDspNodeIp);
        dspNodesList.remove(firstDspNodeIp);
        secondDspNodeIp = dspNodesList.get(random.nextInt(dspNodesList.size()));
        log.info("second dsp node: {}", secondDspNodeIp);
        dspNodesList.remove(secondDspNodeIp);
        informDspNodes();
    }

    private void informDspNodes() {
        informDspNode(firstDspNodeIp, true);
        informDspNode(secondDspNodeIp, true);
        dspNodesList.forEach(dspNode -> informDspNode(dspNode, false));
    }

    private void informDspNode(String dspNode, boolean ifToPropagate) {
        String url = dspNode + "/informDspIfTpPropagate";
        InformDspNodeRequest request = new InformDspNodeRequest();
        request.setToPropagate(ifToPropagate);
        try {
            //restTemplate.put(url, request);
        } catch (RestClientException e) {
            log.error("Errors when propagating to url {} {}", url);
        }
    }


}
