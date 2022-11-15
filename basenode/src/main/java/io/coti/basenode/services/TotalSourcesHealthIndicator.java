package io.coti.basenode.services;

import io.coti.basenode.data.HealthMetricData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class TotalSourcesHealthIndicator implements HealthIndicator {

    @Autowired
    private BaseNodeMonitorService monitorService;

    @Override
    public Health health() {
        Health.Builder builder;
        HealthMetricData healthMetricData = monitorService.getHealthMetricData(HealthMetric.TOTAL_TRANSACTIONS);
        if (healthMetricData.getLastHealthState().ordinal() == BaseNodeMonitorService.HealthState.CRITICAL.ordinal()) {
            builder = new Health.Builder().down();
        } else {
            builder = new Health.Builder().up();
        }
        healthMetricData.getAdditionalValues().forEach((key, value) -> builder.withDetail(key, value.toString()));
        return builder.build();
    }

}
