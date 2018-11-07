package io.coti.trustscore.database;

import io.coti.basenode.database.RocksDBConnector;
import io.coti.trustscore.model.BucketTransactionEvents;
import io.coti.trustscore.model.TransactionEvents;
import io.coti.trustscore.model.TrustScores;
import io.coti.trustscore.model.UserBehaviourEvents;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Primary
@Service
public class TrustScoreRocksDBConnector extends RocksDBConnector {

    @Override
    public void setColumnFamily() {
        super.setColumnFamily();
        columnFamilyClassNames.addAll(Arrays.asList(
                BucketTransactionEvents.class.getName(),
                UserBehaviourEvents.class.getName(),
                TrustScores.class.getName(),
                TransactionEvents.class.getName()
        ));


    }
}
