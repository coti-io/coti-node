package io.coti.basenode.healthindicators;

import io.coti.basenode.services.interfaces.IMonitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import static io.coti.basenode.constants.BaseNodeHealthMetricConstants.TOTAL_POSTPONED_TRANSACTIONS_LABEL;

@Component
public class TotalPostponedTransactionsHealthIndicator implements HealthIndicator {

    @Autowired
    private IMonitorService monitorService;

    @Override
    public Health health() {
        return monitorService.getHealthBuilder(TOTAL_POSTPONED_TRANSACTIONS_LABEL);
    }

}