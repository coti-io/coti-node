package io.coti.financialserver.database;

import io.coti.basenode.database.BaseNodeRocksDBConnector;
import io.coti.financialserver.model.*;
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
                Disputes.class.getName(),
                ConsumerDisputes.class.getName(),
                MerchantDisputes.class.getName(),
                TransactionDisputes.class.getName(),
                ArbitratorDisputes.class.getName(),
                DisputeComments.class.getName(),
                DisputeDocuments.class.getName(),
                ReceiverBaseTransactionOwners.class.getName(),
                RecourseClaims.class.getName(),
                MerchantRollingReserves.class.getName(),
                RecourseClaims.class.getName(),
                RollingReserveReleaseDates.class.getName(),
                DisputeEvents.class.getName(),
                DisputeHistory.class.getName(),
                UnreadUserDisputeEvents.class.getName(),
                InitialFunds.class.getName(),
                TokenSaleDistributions.class.getName()
        ));
    }
}
