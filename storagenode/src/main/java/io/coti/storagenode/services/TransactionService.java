package io.coti.storagenode.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;

@Slf4j
@Service
public class TransactionService extends ObjectService {
    @PostConstruct
    private void init() throws Exception {
        try {
            indexName = "transactions";
            objectName = "transactionData";
            dbConnectorService.addIndex(indexName, objectName, false);
            dbConnectorService.addIndex(indexName, objectName, true);
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new Exception(e.getMessage());
        }
    }
}
