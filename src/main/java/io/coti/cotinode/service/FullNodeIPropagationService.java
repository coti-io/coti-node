package io.coti.cotinode.service;

import io.coti.cotinode.data.Hash;
import io.coti.cotinode.data.TransactionData;
import io.coti.cotinode.http.GetTransactionsResponse;
import io.coti.cotinode.http.Response;
import io.coti.cotinode.http.interfaces.IPropagationSender;
import io.coti.cotinode.service.interfaces.IFullNodeIPropagationService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

@Data
@Slf4j
@Service
public class FullNodeIPropagationService implements IFullNodeIPropagationService {

    @Value("${nodes.file}")
    private String dspNodesFile;

    @Value("${last.index.file}")
    private String lastIndexFile;

    @Autowired
    private IPropagationSender propagationSender;

    private List<String> dspNodesList;
    private String mainDspNodeIp;
    private String secondDspNodeIp;
    private int lastIndex;


    @PostConstruct
    private void init() {
        log.info("full node Propagation service Started");
        dspNodesList = new ArrayList<>();
        initDspNodes();
        loadLastIndex();
    }

    private void loadLastIndex() {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(lastIndexFile).getFile());
        try {
            lastIndex = Integer.parseInt(FileUtils.readFileToString(file).trim());
        } catch (IOException e) {
            log.error("An error while loading the current node",e);
        }
    }


    @Override
    public void propagateToDspNode(TransactionData transactionData) {
        propagationSender.propagateTransactionToNeighbor(transactionData, mainDspNodeIp);
    }

    @Override
    public ResponseEntity<Response> propagateFromDspNode(Hash transactionHash, String dspNode) {
       return propagationSender.propagateTransactionFromNeighbor(transactionHash, dspNode);
    }

    @Override
    public GetTransactionsResponse propagateAllFromDspNode(int index) {
        return propagationSender.propagateMultiTransactionFromNeighbor(index, mainDspNodeIp);
    }

    public void initDspNodes() {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(dspNodesFile).getFile());
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                dspNodesList.add(scanner.nextLine().trim());
            }
        }
        catch (Exception ex){
            log.error("An error while loading the nodesList", ex);
        }
        Random random =new Random();
        mainDspNodeIp = dspNodesList.get(random.nextInt(dspNodesList.size()));
        dspNodesList.remove(mainDspNodeIp );
        secondDspNodeIp= dspNodesList.get(random.nextInt(dspNodesList.size()));

    }

}
