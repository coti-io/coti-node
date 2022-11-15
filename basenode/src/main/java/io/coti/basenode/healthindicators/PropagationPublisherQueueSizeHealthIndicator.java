package io.coti.basenode.healthindicators;

import io.coti.basenode.services.interfaces.IMonitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import static io.coti.basenode.http.BaseNodeHealthMetricConstants.PROPAGATION_PUBLISHER_QUEUE_SIZE_LABEL;

@Component
public class PropagationPublisherQueueSizeHealthIndicator implements HealthIndicator {

    @Autowired
    protected IMonitorService monitorService;

    @Override
    public Health health() {
        return monitorService.getHealthBuilder(PROPAGATION_PUBLISHER_QUEUE_SIZE_LABEL);
    }

}