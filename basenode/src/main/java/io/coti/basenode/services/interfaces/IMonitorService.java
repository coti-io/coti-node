package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.HealthMetricData;
import io.coti.basenode.services.BaseNodeMonitorService;
import io.coti.basenode.services.HealthMetric;
import org.springframework.boot.actuate.health.Health;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;

public interface IMonitorService {

    void init();

    Health getHealthBuilder(String label);

    BaseNodeMonitorService.HealthState getLastTotalHealthState();

    HealthMetricData getHealthMetricData(HealthMetric healthMetric);

    HealthMetricData getHealthMetricData(String label);

    void setMetricValue(HealthMetric label, long metricValue);

    void setSnapshotTime(HealthMetric healthMetric, Instant snapshotTime);

    void updateHealthMetrics(ArrayList<String> metrics, HashMap<String, String> metricTemplateMap);
}
