package io.coti.basenode.data;

import io.coti.basenode.services.BaseNodeMonitorService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;

@Slf4j
@Data
public class HealthMetricData {

    private long lastMetricValue;
    private long lastConditionValue;
    private BaseNodeMonitorService.HealthState lastHealthState;
    private int lastCounter;
    private HashMap<String, Long> additionalValues = new HashMap<>();

    public HealthMetricData(long lastMetricValue, long lastConditionValue, BaseNodeMonitorService.HealthState lastHealthState, int lastCounter) {
        this.lastMetricValue = lastMetricValue;
        this.lastConditionValue = lastConditionValue;
        this.lastHealthState = lastHealthState;
        this.lastCounter = lastCounter;
    }

    public void setSpecificLastMetricValue(String fieldKey, long metricValue) {
        this.additionalValues.put(fieldKey, metricValue);
    }

    public Long getSpecificLastMetricValue(String fieldKey) {
        return this.additionalValues.get(fieldKey);
    }
}
