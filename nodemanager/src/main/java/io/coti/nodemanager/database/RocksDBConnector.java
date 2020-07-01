package io.coti.nodemanager.database;

import io.coti.basenode.database.BaseNodeRocksDBConnector;
import io.coti.basenode.model.GeneralVoteResults;
import io.coti.nodemanager.model.*;
import org.rocksdb.RocksDB;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
@Primary
public class RocksDBConnector extends BaseNodeRocksDBConnector {

    @Value("${reset.nodehistory: false}")
    private boolean resetNodeHistory;

    @Override
    protected void setColumnFamily() {
        columnFamilyClassNames = Arrays.asList(
                new String(RocksDB.DEFAULT_COLUMN_FAMILY),
                ActiveNodes.class.getName(),
                NodeHistory.class.getName(),
                NodeDailyActivities.class.getName(),
                StakingNodes.class.getName(),
                ReservedHosts.class.getName(),
                GeneralVoteResults.class.getName());
    }

    @Override
    protected void populateResetColumnFamilyNames() {
        if (resetNodeHistory) {
            resetColumnFamilyNames.addAll(Arrays.asList(
                    NodeHistory.class.getName(),
                    NodeDailyActivities.class.getName()
            ));
        }
    }
}
