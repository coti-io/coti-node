package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.HealthMetricData;
import org.springframework.boot.actuate.health.Health;

public interface IHealthMetric {
    public void doSnapshot();

    public void calculateHealthMetric();

    public HealthMetricData getHealthMetricData();

    public Health getHealthBuilder(Health.Builder builder);
}
