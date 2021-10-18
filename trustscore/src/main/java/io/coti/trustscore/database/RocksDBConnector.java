package io.coti.trustscore.database;

import io.coti.basenode.database.BaseNodeRocksDBConnector;
import io.coti.trustscore.model.BucketEvents;
import io.coti.trustscore.model.MerchantRollingReserveAddresses;
import io.coti.trustscore.model.TrustScores;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Primary
@Service
public class RocksDBConnector extends BaseNodeRocksDBConnector {

    @Override
    public void setColumnFamily() {
        super.setColumnFamily();
        columnFamilyClassNames.addAll(Arrays.asList(
                BucketEvents.class.getName(),
                TrustScores.class.getName(),
                MerchantRollingReserveAddresses.class.getName()
        ));
    }
}
