package io.coti.basenode.healthindicators;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import static io.coti.basenode.services.BaseNodeServiceManager.monitorService;
import static io.coti.basenode.services.HealthMetric.WAITING_MISSING_TRANSACTION_INDEXES;

@Component
public class WaitingMissingTransactionIndexesHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        return monitorService.getHealthBuilder(WAITING_MISSING_TRANSACTION_INDEXES);
    }
}