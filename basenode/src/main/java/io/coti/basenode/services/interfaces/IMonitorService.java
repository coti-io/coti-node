package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.HealthMetricData;
import io.coti.basenode.data.HealthState;
import io.coti.basenode.services.HealthMetric;
import org.springframework.boot.actuate.health.Health;

import java.time.Instant;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public interface IMonitorService {

    void init();

    Health getHealthBuilder(HealthMetric healthMetric);

    HealthState getLastTotalHealthState();

    HealthMetricData getHealthMetricData(HealthMetric healthMetric);

    void setMetricValue(HealthMetric healthMetric, long metricValue);

    void setSnapshotTime(HealthMetric healthMetric, Instant snapshotTime);

    ReentrantReadWriteLock getMonitorReadWriteLock();

    void initNodeMonitor();

    boolean monitoringStarted();
}
