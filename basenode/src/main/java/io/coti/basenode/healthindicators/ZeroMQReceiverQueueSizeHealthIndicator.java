package io.coti.basenode.healthindicators;

import io.coti.basenode.services.interfaces.IMonitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import static io.coti.basenode.constants.BaseNodeHealthMetricConstants.ZERO_MQ_RECEIVER_QUEUE_SIZE_LABEL;

@Component
public class ZeroMQReceiverQueueSizeHealthIndicator implements HealthIndicator {

    @Autowired
    protected IMonitorService monitorService;

    @Override
    public Health health() {
        return monitorService.getHealthBuilder(ZERO_MQ_RECEIVER_QUEUE_SIZE_LABEL);
    }

}