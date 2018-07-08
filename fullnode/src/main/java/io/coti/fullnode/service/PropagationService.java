package io.coti.fullnode.service;

import io.coti.common.data.Hash;
import io.coti.common.data.TransactionData;
import io.coti.common.http.GetTransactionRequest;
import io.coti.common.http.GetTransactionsRequest;
import io.coti.common.http.Response;
import io.coti.common.model.Transactions;
import io.coti.fullnode.service.interfaces.IPropagationService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

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
    private IPropagationSender propagationSender;

    private List<String> neighborsNodeIps;
    private String currentNodeIp;

    @PostConstruct
    private void init() {
        log.info("Propagation io.coti.fullnode.service Started");
        neighborsNodeIps = new ArrayList<>();
        loadNodesList();
        loadCurrentNode();
    }

    public void propagateToNeighbors(TransactionData transactionData) {
        transactionData.getValidByNodes().put(currentNodeIp, true);
        for (String nodeIp : neighborsNodeIps) {
            propagationSender.propagateTransactionToNeighbor(transactionData, nodeIp);
        }
    }


    public void propagateFromNeighbors(Hash transactionHash) {
        for (String nodeIp : neighborsNodeIps) {
            propagationSender.propagateTransactionFromNeighbor(transactionHash, nodeIp);
        }
    }

    public ResponseEntity<TransactionData> getTrasaction(TransactionData transactionData) {
        List<TransactionData> transactionDatas = null;
        // TODO: Implementing getting all transaction, or from the attachment time;
//        if (transactionData == null) {
//            propagateFromNeighbors(getTransactionRequest);
//            return ResponseEntity.status(HttpStatus.NO_CONTENT)
//                    .body(new AddTransactionResponse(
//                            STATUS_SUCCESS,
//                            TRANSACTION_CURRENTLY_MISSING_MESSAGE));
//        }
//        return ResponseEntity.status(HttpStatus.OK)
//                .body(new GetTransactionResponse(transactions.getByHash(getTransactionRequest.transactionHash)));
        return null;
    }

    public ResponseEntity getTransaction(GetTransactionRequest getTransactionRequest) {
        TransactionData transactionData = transactions.getByHash(getTransactionRequest.transactionHash);
        if (transactionData != null) {
            propagateToNeighbors(transactionData);
        } else {
            for (String neighborIp :
                    neighborsNodeIps) {


            }
            propagateFromNeighbors(getTransactionRequest.transactionHash);
        }
        return new ResponseEntity(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Response> getTransactionsFromCurrentNode(GetTransactionsRequest getTransactionsRequest) {
        return null;
    }

    public void loadNodesList() {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(nodesFile).getFile());
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                neighborsNodeIps.add(scanner.nextLine().trim());
            }
        } catch (Exception ex) {
            log.error("An error while loading the nodesList", ex);
        }
    }


    public void loadCurrentNode() {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(currentNodeFile).getFile());
        try {
            currentNodeIp = FileUtils.readFileToString(file).trim();
        } catch (IOException e) {
            log.error("An error while loading the current node", e);
        }
    }

}
