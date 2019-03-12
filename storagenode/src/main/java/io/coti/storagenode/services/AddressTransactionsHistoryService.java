package io.coti.storagenode.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;

@Slf4j
@Service
public class AddressTransactionsHistoryService extends ObjectService {//} implements IAddressService {

    @PostConstruct
    private void init() throws Exception {
        try {
            indexName = "address";
            objectName = "addressTransactionsHistoryData";
            dbConnectorService.addIndex(indexName, objectName, false);
            dbConnectorService.addIndex(indexName, objectName, true);
        } catch (IOException e) {
            log.warn("Make sure you are running Elasticsearch!");
            log.error(e.getMessage());
            throw new Exception(e.getMessage());
        }
    }
}
