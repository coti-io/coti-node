package io.coti.financialserver.database;

import io.coti.basenode.database.BaseNodeRocksDBConnector;
import io.coti.financialserver.model.*;

import java.util.Arrays;

public class RocksDBConnector extends BaseNodeRocksDBConnector {

    @Override
    public void setColumnFamily() {
        super.setColumnFamily();
        columnFamilyClassNames.addAll(Arrays.asList(
                Disputes.class.getName(),
                DisputeComments.class.getName(),
                DisputeDocuments.class.getName(),
                ReceiverBaseTransactionOwners.class.getName(),
                RecourseClaims.class.getName(),
                RollingReserveAddresses.class.getName(),
                RollingReserveReleaseDates.class.getName()
        ));


    }
}
