package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.HealthMetricData;

public interface IHealthMetric {
    public void doSnapshot();

    public void calculateHealthMetric();

    public HealthMetricData getHealthMetricData();
}
