package io.coti.basenode.healthindicators;

import io.coti.basenode.services.interfaces.IMonitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import static io.coti.basenode.constants.BaseNodeHealthMetricConstants.PROPAGATION_SUBSCRIBER_NETWORK_QUEUE_SIZE_LABEL;

@Component
public class PropagationSubscriberNetworkQueueSizeHealthIndicator implements HealthIndicator {

    @Autowired
    protected IMonitorService monitorService;

    @Override
    public Health health() {
        return monitorService.getHealthBuilder(PROPAGATION_SUBSCRIBER_NETWORK_QUEUE_SIZE_LABEL);
    }

}
