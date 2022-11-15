package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.HealthMetricData;
import io.coti.basenode.services.BaseNodeMonitorService;
import io.coti.basenode.services.HealthMetric;
import org.springframework.boot.actuate.health.Health;

import java.util.Map;

public interface IMonitorService {

    void init();

    Health getHealthBuilder(String label);

    BaseNodeMonitorService.HealthState getLastTotalHealthState();

    void lastState();

    HealthMetricData getHealthMetricData(HealthMetric healthMetric);

    HealthMetricData getHealthMetricData(String label);

    Map<HealthMetric, HealthMetricData> getHealthMetrics();

    void setLastMetricValue(HealthMetric label, long metricValue);

    void setSnapshotTime(HealthMetric healthMetric, String snapshotTime);

    long getSpecificLastMetricValue(HealthMetric healthMetric, String fieldKey);

    void setSpecificLastMetricValue(HealthMetric healthMetric, String fieldKey, long metricValue);
}
