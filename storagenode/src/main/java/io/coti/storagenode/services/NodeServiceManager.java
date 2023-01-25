package io.coti.storagenode.services;

import io.coti.basenode.services.BaseNodeServiceManager;
import io.coti.storagenode.database.DbConnectorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Primary
@SuppressWarnings({"java:S1104", "java:S1444"})
public class NodeServiceManager extends BaseNodeServiceManager {

    public static DbConnectorService dbConnectorService;
    public static ObjectService objectService;
    public static TransactionStorageService transactionStorageService;
    public static AddressStorageService addressStorageService;

    @Autowired
    public DbConnectorService autowiredDbConnectorService;
    @Autowired
    public ObjectService autowiredObjectService;
    @Autowired
    public TransactionStorageService autowiredTransactionStorageService;
    @Autowired
    public AddressStorageService autowiredAddressStorageService;

}
