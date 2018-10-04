package io.coti.trustscore.database;

import io.coti.basenode.database.RocksDBConnector;
import io.coti.trustscore.model.TransactionEvents;
import io.coti.trustscore.model.TrustScoresUsers;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Primary
@Service
public class TrustScoreRocksDBConnector extends RocksDBConnector {


    public TrustScoreRocksDBConnector(){
        addTrustScoreColumnFamilies();
    }



    private void addTrustScoreColumnFamilies(){
        columnFamilyClassNames.add(TrustScoresUsers.class.getName());
        columnFamilyClassNames.add(TransactionEvents.class.getName());

    }

    @Override
    public void init() {
    }
}
