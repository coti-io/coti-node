package io.coti.basenode.services;

import io.coti.basenode.services.interfaces.IClusterService;
import io.coti.basenode.services.interfaces.IMonitorService;
import io.coti.basenode.services.interfaces.ITransactionHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class HealthMetricInjector {
    @Autowired
    protected ITransactionHelper transactionHelper;
    @Autowired
    protected IMonitorService monitorService;
    @Autowired
    protected IClusterService clusterService;

    @PostConstruct
    public void init() {
        HealthMetric.TOTAL_TRANSACTIONS.monitorService = monitorService;
        HealthMetric.TOTAL_TRANSACTIONS.transactionHelper = transactionHelper;

        HealthMetric.SOURCES_UPPER_BOUND.monitorService = monitorService;
        HealthMetric.SOURCES_UPPER_BOUND.clusterService = clusterService;
    }
}
