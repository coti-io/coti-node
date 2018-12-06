package io.coti.financialserver.database;

import io.coti.basenode.database.BaseNodeRocksDBConnector;
import io.coti.financialserver.model.*;

import java.util.Arrays;

public class RocksDBConnector extends BaseNodeRocksDBConnector {

    private static BaseNodeRocksDBConnector connector;

    private RocksDBConnector() {

    }

    public static BaseNodeRocksDBConnector getConnector() {
        if( connector == null ) {
            connector = new RocksDBConnector();
            connector.init("initialDatabase");
        }

        return connector;
    }

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
