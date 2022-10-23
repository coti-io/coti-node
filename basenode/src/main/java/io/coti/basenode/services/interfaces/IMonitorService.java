package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.HealthMetricData;
import io.coti.basenode.services.BaseNodeMonitorService;
import io.coti.basenode.services.HealthMetric;

public interface IMonitorService {

    void init();

    BaseNodeMonitorService.HealthState getLastTotalHealthState();

    void lastState();

    HealthMetricData getHealthMetricData(HealthMetric healthMetric);

    void setLastMetricValue(HealthMetric label, long metricValue);

    long getSpecificLastMetricValue(HealthMetric healthMetric, String fieldKey);

    void setSpecificLastMetricValue(HealthMetric healthMetric, String fieldKey, long metricValue);
}
