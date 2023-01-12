package io.coti.basenode.healthindicators;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import static io.coti.basenode.services.BaseNodeServiceManager.monitorService;
import static io.coti.basenode.services.HealthMetric.DCR_CONFIRMATION_QUEUE_SIZE;

@Component
public class DcrConfirmationQueueSizeHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        return monitorService.getHealthBuilder(DCR_CONFIRMATION_QUEUE_SIZE);
    }

}