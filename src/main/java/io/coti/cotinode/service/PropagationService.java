package io.coti.cotinode.service;

import io.coti.cotinode.data.TransactionData;
import io.coti.cotinode.http.*;
import io.coti.cotinode.http.interfaces.IPropagationCommunication;
import io.coti.cotinode.model.Transactions;
import io.coti.cotinode.service.interfaces.IPropagationService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static io.coti.cotinode.http.HttpStringConstants.STATUS_SUCCESS;
import static io.coti.cotinode.http.HttpStringConstants.TRANSACTION_CURRENTLY_MISSING_MESSAGE;

@Data
@Slf4j
@Service
public class PropagationService implements IPropagationService {
    @Value("${nodes.file}")
    private String nodesFile;

    @Value("${current.node.file}")
    private String currentNodeFile;

    @Autowired
    private Transactions transactions;

    @Autowired
    private IPropagationCommunication propagationCommunication;

    private List<String> NeighborsNodesIp;
    private String currentNodeIp;

    @PostConstruct
    private void init() {
        log.info("Propagation service Started");
        NeighborsNodesIp = new ArrayList<>();
        loadNodesList();
        loadCurrentNode();
    }

    public void propagateToNeighbors(AddTransactionRequest request) {
        request.transactionData.getValidByNodes().put(currentNodeIp, true);
        for (String nodeIp : NeighborsNodesIp) {
            propagationCommunication.propagateTransactionToNeighbor(request, nodeIp);
        }
    }


    public void propagateFromNeighbors(GetTransactionRequest getTransactionRequest) {
        for (String nodeIp : NeighborsNodesIp) {
            propagationCommunication.propagateTransactionFromNeighbor(getTransactionRequest, nodeIp);
        }
    }


    public ResponseEntity<Response> getTransactionFromCurrentNode(GetTransactionRequest getTransactionRequest) {
        TransactionData transactionData = transactions.getByHash(getTransactionRequest.transactionHash);
        if (transactionData == null) {
            propagateFromNeighbors(getTransactionRequest);
            return ResponseEntity.status(HttpStatus.NO_CONTENT)
                    .body(new AddTransactionResponse(
                            STATUS_SUCCESS,
                            TRANSACTION_CURRENTLY_MISSING_MESSAGE));
        }
        return ResponseEntity.status(HttpStatus.OK)
                .body(new GetTransactionResponse(transactions.getByHash(getTransactionRequest.transactionHash)));
    }

    public void loadNodesList() {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(nodesFile).getFile());
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                NeighborsNodesIp.add(scanner.nextLine().trim());
            }
        }
        catch (Exception ex){
            log.error("An error while loading the nodesList", ex);
        }
    }


    public void loadCurrentNode() {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(currentNodeFile).getFile());
        try {
            currentNodeIp = FileUtils.readFileToString(file).trim();
        } catch (IOException e) {
            log.error("An error while loading the current node",e);
        }
    }

}
