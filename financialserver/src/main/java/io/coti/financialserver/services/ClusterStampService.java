package io.coti.financialserver.services;

import io.coti.basenode.data.ClusterStampData;
import io.coti.basenode.services.BaseNodeClusterStampService;
import io.coti.financialserver.model.InitialFundsHashes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * A service that provides Cluster Stamp functionality for Financial Server.
 */
@Slf4j
@Service
public class ClusterStampService extends BaseNodeClusterStampService {


    @Autowired
    DistributionService distributionService;
    @Autowired
    InitialFundsHashes initialFundsHashes;
    @Autowired
    RollingReserveService rollingReserveService;


    @Override
    public void loadBalanceFromLastClusterStamp() {

        ClusterStampData clusterStampData = getLastClusterStamp();

        if(clusterStampData != null) {
            loadBalanceFromClusterStamp(clusterStampData);
            initialFundsHashes.init();

            distributionService.distributeToInitialFunds();
            // TODO: Verify in case Transactions Index is still lower than expected initial transactions,
            //  increment it in order to avoid regular transactions from getting them


//          distributionService.startLoadDistributionsFromJsonFileThread();
        }
    }
}