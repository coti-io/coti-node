package io.coti.basenode.healthindicators;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import static io.coti.basenode.services.BaseNodeServiceManager.monitorService;
import static io.coti.basenode.services.HealthMetric.PROPAGATION_SUBSCRIBER_HEARTBEAT_QUEUE_SIZE;

@Component
public class PropagationSubscriberHeartbeatQueueSizeHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        return monitorService.getHealthBuilder(PROPAGATION_SUBSCRIBER_HEARTBEAT_QUEUE_SIZE);
    }

}