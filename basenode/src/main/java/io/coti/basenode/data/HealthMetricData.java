package io.coti.basenode.data;

import io.coti.basenode.services.BaseNodeMonitorService;
import io.coti.basenode.services.HealthMetric;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Data
public class HealthMetricData {

    private long metricValue;
    private long previousMetricValue;
    private Instant snapshotTime;
    private BaseNodeMonitorService.HealthState lastHealthState;
    private int degradingCounter;
    private Map<String, HealthMetricOutput> additionalValues = new HashMap<>();
    @Getter
    private Long warningThreshold;
    @Getter
    private Long criticalThreshold;


    public HealthMetricData(long metricValue, long previousMetricValue, BaseNodeMonitorService.HealthState lastHealthState, int degradingCounter, Instant snapshotTime) {
        this.metricValue = metricValue;
        this.previousMetricValue = previousMetricValue;
        this.lastHealthState = lastHealthState;
        this.degradingCounter = degradingCounter;
        this.snapshotTime = snapshotTime;
    }

    public HealthMetricData(HealthMetric healthMetric) {
        this.metricValue = 0;
        this.previousMetricValue = 0;
        this.lastHealthState = BaseNodeMonitorService.HealthState.NA;
        this.degradingCounter = 0;
        this.warningThreshold = healthMetric.getDefaultWarningThreshold();
        this.criticalThreshold = healthMetric.getDefaultCriticalThreshold();
    }

    public void setSpecificLastMetricValue(String fieldKey, HealthMetricOutput healthMetricOutput) {
        this.additionalValues.put(fieldKey, healthMetricOutput);
    }

    public Long getSpecificLastMetricValue(String fieldKey) {
        return Optional.of(this.additionalValues.get(fieldKey).getValue()).orElse((long) -1);
    }

    public void increaseDegradingCounter() {
        this.degradingCounter += 1;
    }

    public void addValue(String metricName, HealthMetricOutputType healthMetricOutputType, String metricLabel, long metricValue) {
        HealthMetricOutput healthMetricOutput = additionalValues.get(metricName);
        if (healthMetricOutput == null) {
            healthMetricOutput = new HealthMetricOutput(healthMetricOutputType, metricLabel, metricValue);
            additionalValues.put(metricName, healthMetricOutput);
        } else {
            healthMetricOutput.setType(healthMetricOutputType);
            healthMetricOutput.setLabel(metricLabel);
            healthMetricOutput.setValue(metricValue);
        }
    }
}
