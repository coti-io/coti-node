package io.coti.financialserver.services;

import io.coti.basenode.services.BaseNodeClusterStampService;
import io.coti.financialserver.model.InitialFunds;
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
    InitialFunds initialFundsHashes;
    @Autowired
    RollingReserveService rollingReserveService;


    @Override
    public void loadClusterStamp() throws Exception {

        super.loadClusterStamp();
     //   distributionService.distributeToInitialFunds();

    }
}