package io.coti.basenode.services.interfaces;

import io.coti.basenode.services.HealthMetric;
import io.coti.basenode.data.HealthMetricData;

public interface IMonitorService {

    void init();

    void lastState();

    HealthMetricData getHealthMetricData(HealthMetric healthMetric);

    void setLastMetricValue(HealthMetric label, long metricValue);

    long getSpecificLastMetricValue(HealthMetric healthMetric, String fieldKey);

    void setSpecificLastMetricValue(HealthMetric healthMetric, String fieldKey, long metricValue);
}
