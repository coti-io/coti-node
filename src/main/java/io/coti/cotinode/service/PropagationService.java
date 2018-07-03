package io.coti.cotinode.service;

import io.coti.cotinode.data.Hash;
import io.coti.cotinode.http.AddTransactionRequest;
import io.coti.cotinode.service.interfaces.IPropagationService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
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

    private List<String> nodesIp;
    private String currentNodeIp;

    @PostConstruct
    private void init() {
        log.info("Propagation service Started");
        nodesIp = new ArrayList<>();
        loadNodesList();
        loadCurrentNode();
    }

    public void propagateToNeighbors(AddTransactionRequest request) {

    }

    public void getTransactionFromNeighbors(Hash transactionHash) {

    }


    private void loadNodesList() {

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(nodesFile).getFile());
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                nodesIp.add(scanner.nextLine().trim());
            }
        }
        catch (Exception ex){
            log.error("An error while loading the nodesList", ex);
        }
    }


    private void loadCurrentNode(){
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(currentNodeFile).getFile());
        try {
            currentNodeIp = FileUtils.readFileToString(file).trim();
        } catch (IOException e) {
            log.error("An error while loading the current node",e);
        }
    }

}
