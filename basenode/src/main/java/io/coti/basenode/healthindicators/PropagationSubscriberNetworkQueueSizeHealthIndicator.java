package io.coti.basenode.healthindicators;

import io.coti.basenode.services.HealthMetric;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import static io.coti.basenode.services.BaseNodeServiceManager.monitorService;

@Component
public class PropagationSubscriberNetworkQueueSizeHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        return monitorService.getHealthBuilder(HealthMetric.PROPAGATION_SUBSCRIBER_NETWORK_QUEUE_SIZE);
    }

}
