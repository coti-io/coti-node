package io.coti.cotinode.model;

import io.coti.cotinode.data.TransactionData;
import io.coti.cotinode.storage.RocksDBConnector;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;

@Slf4j
public class Transactions extends Collection<TransactionData> {

    public Transactions(){
        init();
        dataObjectClass = TransactionData.class;
    }

    @PostConstruct
    public void init(){
        log.info("Initializing collection!");
        databaseConnector = new RocksDBConnector();
        databaseConnector.init();
    }
}
