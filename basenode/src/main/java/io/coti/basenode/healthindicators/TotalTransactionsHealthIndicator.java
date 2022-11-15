package io.coti.basenode.healthindicators;

import io.coti.basenode.data.HealthMetricData;
import io.coti.basenode.services.BaseNodeMonitorService;
import io.coti.basenode.services.HealthMetric;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class TotalTransactionsHealthIndicator implements HealthIndicator {

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
        builder.withDetail(HealthMetric.TOTAL_TRANSACTIONS.label, healthMetricData.getLastMetricValue());
        healthMetricData.getAdditionalValues().forEach((key, value) -> builder.withDetail(key, value.toString()));
        return builder.build();
    }

}
