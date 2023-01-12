package io.coti.basenode.healthindicators;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import static io.coti.basenode.services.BaseNodeServiceManager.monitorService;
import static io.coti.basenode.services.HealthMetric.NUMBER_OF_LIVE_FILES_NOT_BACKED_UP;


@Component
public class NumberOfLivesFilesNotBackedUpHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        return monitorService.getHealthBuilder(NUMBER_OF_LIVE_FILES_NOT_BACKED_UP);
    }

}
