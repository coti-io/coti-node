package io.coti.trustscore.testutils;

import io.coti.trustscore.database.TrustScoreRocksDBConnector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class DBInitializerService {

    @Autowired
    private TrustScoreRocksDBConnector trustScoreRocksDBConnector;

    @PostConstruct
    public void init() {
        trustScoreRocksDBConnector.init();
    }
}
