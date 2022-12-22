package io.coti.basenode.healthindicators;

import io.coti.basenode.services.interfaces.IMonitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import static io.coti.basenode.constants.BaseNodeHealthMetricConstants.TOTAL_TRANSACTIONS_DELTA_LABEL;

@Component
public class TotalTransactionsHealthIndicator implements HealthIndicator {

    @Autowired
    protected IMonitorService monitorService;

    @Override
    public Health health() {
        return monitorService.getHealthBuilder(TOTAL_TRANSACTIONS_DELTA_LABEL);
    }

}
