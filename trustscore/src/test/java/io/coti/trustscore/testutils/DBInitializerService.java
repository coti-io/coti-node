package io.coti.trustscore.testutils;

import io.coti.trustscore.database.RocksDBConnector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class DBInitializerService {

    @Autowired
    private RocksDBConnector rocksDBConnector;

    @PostConstruct
    public void init() {
        rocksDBConnector.init();
    }
}
