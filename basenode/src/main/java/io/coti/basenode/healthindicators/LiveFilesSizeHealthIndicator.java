package io.coti.basenode.healthindicators;

import io.coti.basenode.services.interfaces.IMonitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import static io.coti.basenode.services.HealthMetric.LIVE_FILES_SIZE;

@Component
public class LiveFilesSizeHealthIndicator implements HealthIndicator {

    @Autowired
    private IMonitorService monitorService;

    @Override
    public Health health() {
        return monitorService.getHealthBuilder(LIVE_FILES_SIZE);
    }

}