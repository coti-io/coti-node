package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.HealthMetricData;

public interface IHealthMetric {
    void doSnapshot();

    void calculateHealthMetric();

    HealthMetricData getHealthMetricData();

    String getDescription();

    void setWarningThreshold(long l);

    void setCriticalThreshold(long l);
}
