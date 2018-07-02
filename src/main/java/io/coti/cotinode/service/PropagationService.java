package io.coti.cotinode.service;

import io.coti.cotinode.data.Hash;
import io.coti.cotinode.http.AddTransactionRequest;
import io.coti.cotinode.http.AddTransactionResponse;
import io.coti.cotinode.service.interfaces.IPropagationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Stream;

@Slf4j
@Service
public class PropagationService implements IPropagationService {
    @Value("${nodes.file}")
    private String nodesFile;

    private List<String> nodesIp;

    @PostConstruct
    private void init() {
        log.info("Propagation service Started");
        nodesIp = new ArrayList<>();
        loadNodesList();
    }

    public void propagateToNeighbors(AddTransactionRequest request) {

    }

    public void getTransactionFromNeighbors(Hash leftParentHash) {

    }


    public void loadNodesList() {

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(nodesFile).getFile());

        ClassPathResource res = new ClassPathResource(nodesFile);
        try (Scanner scanner = new Scanner(file)) {

            while (scanner.hasNextLine()) {
                nodesIp.add(scanner.nextLine());
            }
            scanner.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
