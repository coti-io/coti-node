package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.HealthMetricData;
import io.coti.basenode.services.HealthMetric;
import org.springframework.boot.actuate.health.Health;

import java.util.Map;

public interface IHealthMetric {
    public void doSnapshot();

    public void calculateHealthMetric();

    public HealthMetricData getHealthMetricData();

    Map<HealthMetric, HealthMetricData> getHealthMetrics();

    public Health getHealthBuilder(Health.Builder builder);
}
