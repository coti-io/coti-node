package io.coti.basenode.data;

import io.coti.basenode.services.BaseNodeMonitorService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Optional;

@Slf4j
@Data
public class HealthMetricData {

    private long lastMetricValue;
    private long lastConditionValue;
    private String snapshotTime;
    private BaseNodeMonitorService.HealthState lastHealthState;
    private int lastCounter;
    private HashMap<String, Long> additionalValues = new HashMap<>();

    public HealthMetricData(long lastMetricValue, long lastConditionValue, BaseNodeMonitorService.HealthState lastHealthState, int lastCounter, String snapshotTime) {
        this.lastMetricValue = lastMetricValue;
        this.lastConditionValue = lastConditionValue;
        this.lastHealthState = lastHealthState;
        this.lastCounter = lastCounter;
        this.snapshotTime = snapshotTime;
    }

    public HealthMetricData() {
        this.lastMetricValue = 0;
        this.lastConditionValue = 0;
        this.lastHealthState = BaseNodeMonitorService.HealthState.NA;
        this.lastCounter = 0;
    }

    public void setSpecificLastMetricValue(String fieldKey, long metricValue) {
        this.additionalValues.put(fieldKey, metricValue);
    }

    public Long getSpecificLastMetricValue(String fieldKey) {
        return Optional.ofNullable(this.additionalValues.get(fieldKey)).orElse(Long.valueOf(-1));
    }
}
