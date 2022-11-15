package io.coti.basenode.services;

import io.coti.basenode.data.HealthMetricData;
import io.coti.basenode.services.interfaces.IClusterService;
import io.coti.basenode.services.interfaces.IHealthMetric;
import io.coti.basenode.services.interfaces.IMonitorService;
import io.coti.basenode.services.interfaces.ITransactionHelper;

import java.util.Arrays;

public enum HealthMetric implements IHealthMetric {


    TOTAL_TRANSACTIONS("TotalTransactions", true, 0, 0) {
        @Override
        public void doSnapshot() {
            monitorService.setLastMetricValue(this, transactionHelper.getTotalNumberOfTransactionsFromLocal());
            monitorService.getHealthMetricData(this).setSpecificLastMetricValue(TOTAL_TRANSACTIONS_FROM_RECOVERY, transactionHelper.getTotalNumberOfTransactionsFromRecovery());
        }

        @Override
        public void calculateHealthMetric() {
            HealthMetricData healthMetricData = monitorService.getHealthMetricData(this);
            long conditionValue = healthMetricData.getSpecificLastMetricValue(TOTAL_TRANSACTIONS_FROM_RECOVERY) - healthMetricData.getLastMetricValue();
            healthMetricData.setLastConditionValue(conditionValue);
            calculateHealthCounterMetricState(healthMetricData, this);
        }
    },
    SOURCES_UPPER_BOUND("SourcesUpperBound", false, 0, 0) {
        @Override
        public void doSnapshot() {
            monitorService.setLastMetricValue(this, clusterService.getTotalSources());
        }

        @Override
        public void calculateHealthMetric() {
            HealthMetricData healthMetricData = monitorService.getHealthMetricData(this);
            healthMetricData.setLastConditionValue(healthMetricData.getLastMetricValue());
            calculateHealthValueMetricState(healthMetricData, this);
        }
    };

    protected ITransactionHelper transactionHelper;
    protected IMonitorService monitorService;
    protected IClusterService clusterService;

    protected static final String TOTAL_TRANSACTIONS_FROM_RECOVERY = "totalTransactionsFromRecovery";

    public final String label;
    private final boolean counterBased;
    private long warningThreshold;
    private long criticalThreshold;

    HealthMetric(String label, boolean counterBased, long warningThreshold, long criticalThreshold) {
        this.label = label;
        this.counterBased = counterBased;
        this.warningThreshold = warningThreshold;
        this.criticalThreshold = criticalThreshold;
    }

    public void setThresholds(long warningThreshold, long criticalThreshold) {
        this.warningThreshold = warningThreshold;
        this.criticalThreshold = criticalThreshold;
    }

    public static HealthMetric getHealthMetric(String label) {
        return Arrays.stream(HealthMetric.values()).filter(metric -> label.equalsIgnoreCase(metric.label))
                .findFirst().orElseThrow(() -> new IllegalArgumentException("No metric found"));
    }

    @Override
    public HealthMetricData getHealthMetricData() {
        return monitorService.getHealthMetricData(this);
    }

    private static void calculateHealthCounterMetricState(HealthMetricData healthMetricData, HealthMetric healthMetric) {
        if (healthMetricData.getLastConditionValue() > 0) {
            healthMetricData.setLastCounter(healthMetricData.getLastCounter() + 1);
            if (healthMetricData.getLastCounter() >= healthMetric.criticalThreshold &&
                    healthMetric.criticalThreshold >= healthMetric.warningThreshold) {
                healthMetricData.setLastHealthState(BaseNodeMonitorService.HealthState.CRITICAL);
            } else if (healthMetricData.getLastCounter() >= healthMetric.warningThreshold) {
                healthMetricData.setLastHealthState(BaseNodeMonitorService.HealthState.WARNING);
            }
        } else {
            healthMetricData.setLastCounter(0);
            healthMetricData.setLastHealthState(BaseNodeMonitorService.HealthState.NORMAL);
        }
    }

    private static void calculateHealthValueMetricState(HealthMetricData healthMetricData, HealthMetric healthMetric) {
        long lastConditionValue = healthMetricData.getLastConditionValue();
        if (lastConditionValue >= healthMetric.criticalThreshold &&
                healthMetric.criticalThreshold >= healthMetric.warningThreshold) {
            healthMetricData.setLastHealthState(BaseNodeMonitorService.HealthState.CRITICAL);
        } else if (healthMetricData.getLastCounter() >= healthMetric.warningThreshold) {
            healthMetricData.setLastHealthState(BaseNodeMonitorService.HealthState.WARNING);
        } else {
            healthMetricData.setLastHealthState(BaseNodeMonitorService.HealthState.NORMAL);
        }
    }


}
