package io.coti.trustscore.database;

import io.coti.basenode.database.RocksDBConnector;
import io.coti.trustscore.model.*;
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

        columnFamilyClassNames.add(BucketChargeBackEvents.class.getName());
    }
}
