package io.coti.storagenode.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;

@Slf4j
@Service
public class AddressService extends ObjectService {//} implements IAddressService {

    @PostConstruct
    private void init() throws Exception {
        try {
            indexName = "address";
            objectName = "addressData";
            dbConnectorService.addIndex(indexName, objectName);
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new Exception(e.getMessage());
        }
    }
}
