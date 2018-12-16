package io.coti.financialserver.database;

import java.util.Arrays;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import io.coti.basenode.database.BaseNodeRocksDBConnector;
import io.coti.financialserver.model.*;

@Primary
@Service
public class RocksDBConnector extends BaseNodeRocksDBConnector {

    private RocksDBConnector() {

    }

    @Override
    public void setColumnFamily() {
        super.setColumnFamily();
        columnFamilyClassNames.addAll(Arrays.asList(
                Disputes.class.getName(),
                UserDisputes.class.getName(),
                DisputeComments.class.getName(),
                DisputeDocuments.class.getName(),
                FinancialServerTransactions.class.getName(),
                ReceiverBaseTransactionOwners.class.getName(),
                RecourseClaims.class.getName(),
                RollingReserveAddresses.class.getName(),
                RollingReserveReleaseDates.class.getName()
        ));
    }
}
