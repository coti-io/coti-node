package io.coti.basenode.services;

import io.coti.basenode.communication.interfaces.IPropagationPublisher;
import io.coti.basenode.communication.interfaces.IPropagationSubscriber;
import io.coti.basenode.communication.interfaces.IReceiver;
import io.coti.basenode.data.HealthMetricData;
import io.coti.basenode.data.HealthMetricOutput;
import io.coti.basenode.data.HealthMetricOutputType;
import io.coti.basenode.database.interfaces.IDatabaseConnector;
import io.coti.basenode.services.interfaces.*;
import io.coti.basenode.utilities.MonitorConfigurationProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class BaseNodeMonitorService implements IMonitorService {

    private final Map<HealthMetric, HealthMetricData> healthMetrics = new ConcurrentHashMap<>();
    @Autowired
    protected INetworkService networkService;
    @Autowired
    MonitorConfigurationProperties monitorConfigurationProperties;
    @Autowired
    private ITransactionHelper transactionHelper;
    @Autowired
    private IConfirmationService confirmationService;
    @Autowired
    private TransactionIndexService transactionIndexService;
    @Autowired
    private TrustChainConfirmationService trustChainConfirmationService;
    @Autowired
    private IClusterService clusterService;
    @Autowired
    private ITransactionService transactionService;
    @Autowired
    private IPropagationSubscriber propagationSubscriber;
    @Autowired
    private IWebSocketMessageService webSocketMessageService;
    @Autowired
    private IReceiver receiver;
    @Autowired
    private IPropagationPublisher propagationPublisher;
    @Autowired
    private IDatabaseConnector databaseConnector;
    @Autowired
    private IDBRecoveryService dbRecoveryService;
    @Value("${allow.transaction.monitoring:false}")
    private boolean allowTransactionMonitoring;
    @Value("${detailed.logs:false}")
    private boolean allowTransactionMonitoringDetailed;
    private HealthState lastTotalHealthState = HealthState.NA;

    public void init() {
        log.info("{} is up", this.getClass().getSimpleName());
    }

    @PostConstruct
    private void initHealthMetrics() throws InvocationTargetException, IllegalAccessException {
        HealthMetric.setAutowireds(this, transactionHelper, clusterService, transactionIndexService, confirmationService,
                trustChainConfirmationService, transactionService, propagationSubscriber, webSocketMessageService,
                networkService, receiver, databaseConnector, dbRecoveryService, rejectedTransactions, propagationPublisher);

        monitorConfigurationProperties.updateThresholds(HealthMetric.values());
        for (HealthMetric value : HealthMetric.values()) {
            healthMetrics.put(value, new HealthMetricData());
        }
    }

    private void updateHealthMetricsSnapshot() {
        healthMetrics.forEach((healthMetric, metricData) -> healthMetric.doSnapshot()
        );
    }

    private void calculateHealthMetrics() {
        healthMetrics.forEach((healthMetric, metricData) -> healthMetric.calculateHealthMetric()
        );
    }

    private void calculateTotalHealthState() {
        HealthState calculatedTotalHealthState = HealthState.NORMAL;
        for (Map.Entry<HealthMetric, HealthMetricData> entry : healthMetrics.entrySet()) {
            HealthMetricData metricData = entry.getValue();
            if (metricData.getLastHealthState().ordinal() > calculatedTotalHealthState.ordinal()) {
                calculatedTotalHealthState = metricData.getLastHealthState();
            }
        }
        this.lastTotalHealthState = calculatedTotalHealthState;
    }

    private String createHealthStateOutputAsString(StringBuilder output) {
        output.append("TotalHealthState").append(" = ").append(getLastTotalHealthState().toString());
        if (getLastTotalHealthState().ordinal() > HealthState.NORMAL.ordinal()) {
            output.append(", ");
            for (Map.Entry<HealthMetric, HealthMetricData> entry : healthMetrics.entrySet()) {
                HealthMetricData metricData = entry.getValue();
                HealthMetric healthMetric = entry.getKey();
                if (metricData.getLastHealthState().ordinal() > HealthState.NORMAL.ordinal()) {
                    output.append(healthMetric.getLabel()).append(" is ").append(metricData.getLastHealthState().toString());
                    if (metricData.getDegradingCounter() > 0) {
                        output.append(" (counter = ").append(metricData.getDegradingCounter());
                        output.append(", value = ").append(metricData.getMetricValue()).append("), ");
                    } else {
                        output.append("(value = ").append(metricData.getMetricValue()).append("), ");
                    }
                }
            }
            output.deleteCharAt(output.lastIndexOf(","));
        }
        return output.toString();
    }

    @Override
    public HealthMetricData getHealthMetricData(HealthMetric healthMetric) {
        return Optional.ofNullable(healthMetrics.get(healthMetric)).orElseThrow(() -> new IllegalArgumentException("No matching health metric found: ".concat(healthMetric.getLabel())));
    }

    @Override
    public void setMetricValue(HealthMetric healthMetric, long metricValue) {
        getHealthMetricData(healthMetric).setPreviousMetricValue(getHealthMetricData(healthMetric).getMetricValue());
        getHealthMetricData(healthMetric).setMetricValue(metricValue);
    }

    @Override
    public void setSnapshotTime(HealthMetric healthMetric, Instant snapshotTime) {
        getHealthMetricData(healthMetric).setSnapshotTime(snapshotTime);
    }

    @Override
    public Health getHealthBuilder(HealthMetric healthMetric) {
        Health.Builder builder = new Health.Builder();
        HealthMetricData healthMetricData = getHealthMetricData(healthMetric);

        if (!HealthMetric.isToAddExternalMetric(healthMetric.getHealthMetricOutputType()) ||
                healthMetricData.getLastHealthState().ordinal() == HealthState.NA.ordinal()) {
            builder.unknown();
            return builder.build();
        }

        if (healthMetricData.getLastHealthState().ordinal() == HealthState.CRITICAL.ordinal()) {
            builder.down();
        } else {
            builder.up();
        }
        builder.withDetail("State", healthMetricData.getLastHealthState());
        long warningThreshold = healthMetric.getWarningThreshold();
        long criticalThreshold = healthMetric.getCriticalThreshold();
        HashMap<String, Long> configMap = new HashMap<>();
        if (warningThreshold != criticalThreshold) {
            configMap.put("WarningThreshold", warningThreshold);
        }
        if (criticalThreshold >= warningThreshold) {
            configMap.put("CriticalThreshold", criticalThreshold);
        }
        if (!healthMetric.isDetailedLogs() || allowTransactionMonitoringDetailed) {
            builder.withDetail("PreviousMetricValue", healthMetricData.getPreviousMetricValue());
            builder.withDetail("MetricValue", healthMetricData.getMetricValue());
            if (healthMetricData.getDegradingCounter() > 0) {
                builder.withDetail("Counter", healthMetricData.getDegradingCounter());
            }
            healthMetricData.getAdditionalValues().forEach((key, value) -> builder.withDetail(key, value.getValue()));
        }
        builder.withDetail("Configuration", configMap);
        builder.withDetail("Description", healthMetric.getDescription());
        return builder.build();
    }

    @Override
    public HealthState getLastTotalHealthState() {
        return lastTotalHealthState;
    }

    @Scheduled(initialDelay = 1000, fixedDelay = 5000)
    private void lastState() {
        if (allowTransactionMonitoring) {
            updateHealthMetricsSnapshot();
            calculateHealthMetrics();
            calculateTotalHealthState();

            int logLevel = lastTotalHealthState.ordinal();
            printLastState(logLevel);
        }
    }

    private void printLastState(int logLevel) {
        String output = createOutputAsString(allowTransactionMonitoringDetailed);
        printToLogByLevel(logLevel, output);
    }

    private String createOutputAsString(boolean isDetailedLog) {
        StringBuilder output = new StringBuilder();
        for (HealthMetric healthMetric : HealthMetric.values()) {
            if (isDetailedLog || !healthMetric.isDetailedLogs()) {
                for (HealthMetricOutput healthMetricOutput : healthMetric.getHealthMetricData().getAdditionalValues().values()) {
                    if (HealthMetric.isToAddConsoleMetric(healthMetricOutput.getType())) {
                        appendOutput(output, healthMetricOutput.getLabel(), healthMetricOutput.getValue());
                    }
                }
                if (HealthMetric.isToAddConsoleMetric(healthMetric.getHealthMetricOutputType())) {
                    appendOutput(output, healthMetric.getLabel(), healthMetric.getHealthMetricData().getMetricValue());
                }
            }
        }
        return createHealthStateOutputAsString(output);
    }

    private void appendOutput(StringBuilder output, String name, long value) {
        output.append(name).append(" = ").append(value).append(", ");
    }

    private void printToLogByLevel(int logLevel, String logText) {
        if (logLevel == 1) {
            log.info(logText);
        } else if (logLevel == 2) {
            log.warn(logText);
        } else if (logLevel == 3) {
            log.error(logText);
        }
    }

    public enum HealthState {
        NA, NORMAL, WARNING, CRITICAL
    }
}
