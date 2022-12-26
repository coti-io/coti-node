package io.coti.basenode.healthindicators;

import io.coti.basenode.services.HealthMetric;
import io.coti.basenode.services.interfaces.IMonitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class PropagationSubscriberNetworkQueueSizeHealthIndicator implements HealthIndicator {

    @Autowired
    private IMonitorService monitorService;

    @Override
    public Health health() {
        return monitorService.getHealthBuilder(HealthMetric.PROPAGATION_SUBSCRIBER_NETWORK_QUEUE_SIZE);
    }

}
