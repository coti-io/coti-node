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
            INDEX_NAME = "transactions";
            OBJECT_NAME = "transactionData";
            dbConnectorService.addIndex(INDEX_NAME, OBJECT_NAME);
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new Exception(e.getMessage());
        }
    }
}
