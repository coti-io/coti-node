package io.coti.basenode.services;

import io.coti.basenode.communication.ZeroMQSubscriberQueue;
import io.coti.basenode.communication.interfaces.IPropagationSubscriber;
import io.coti.basenode.model.RejectedTransactions;
import io.coti.basenode.data.HealthMetricData;
import io.coti.basenode.services.interfaces.*;
import io.coti.basenode.utilities.MemoryUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class BaseNodeMonitorService implements IMonitorService {

    private final Map<HealthMetric, HealthMetricData> healthMetrics = new ConcurrentHashMap<>();
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
    private RejectedTransactions rejectedTransactions;
    @Autowired
    private IPropagationSubscriber propagationSubscriber;
    @Autowired
    private IWebSocketMessageService webSocketMessageService;
    @Value("${allow.transaction.monitoring}")
    private boolean allowTransactionMonitoring;
    @Value("${detailed.logs:false}")
    private boolean allowTransactionMonitoringDetailed;
    @Value("${dsp.threshold.warning:2}")
    private int dspThresholdWarning;
    @Value("${dsp.threshold.error:5}")
    private int dspThresholdError;
    @Value("${tcc.threshold.warning:5}")
    private int tccThresholdWarning;
    @Value("${tcc.threshold.error:10}")
    private int tccThresholdError;

    @Value("${total.transactions.threshold.warning:1}")
    private int totalTransactionsThresholdWarning;
    @Value("${total.transactions.threshold.critical:2}")
    private int totalTransactionsThresholdCritical;

    @Value("${sources.upperBound.threshold.warning:24}")
    private int sourcesUpperBoundThresholdWarning;
    @Value("${sources.upperBound.threshold.critical:34}")
    private int sourcesUpperBoundThresholdCritical;

    private HealthState lastTotalHealthState = HealthState.NORMAL;

    private long prevDspConfirmed = 0;
    private int dspOutsideNormalCounter = 0;
    private HealthState dspConfirmedState = HealthState.NORMAL;
    private HealthState tccConfirmedState = HealthState.NORMAL;

    public void init() {
        log.info("{} is up", this.getClass().getSimpleName());
    }

    @PostConstruct
    private void initHealthMetrics() {
        HealthMetric.TOTAL_TRANSACTIONS.setThresholds(totalTransactionsThresholdWarning, totalTransactionsThresholdCritical);
        HealthMetric.SOURCES_UPPER_BOUND.setThresholds(sourcesUpperBoundThresholdWarning, sourcesUpperBoundThresholdCritical);

        healthMetrics.put(HealthMetric.TOTAL_TRANSACTIONS, new HealthMetricData(
                0, 0, HealthState.NORMAL, 0));
        healthMetrics.put(HealthMetric.SOURCES_UPPER_BOUND, new HealthMetricData(
                0, 0, HealthState.NORMAL, 0));
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

    @Override
    public HealthMetricData getHealthMetricData(HealthMetric healthMetric) {
        return Optional.ofNullable(healthMetrics.get(healthMetric)).orElseThrow(() -> new IllegalArgumentException("No matching health metric found"));
    }

    @Override
    public void setLastMetricValue(HealthMetric healthMetric, long metricValue) {
        getHealthMetricData(healthMetric).setLastMetricValue(metricValue);
    }

    @Override
    public long getSpecificLastMetricValue(HealthMetric healthMetric, String fieldKey) {
        return getHealthMetricData(healthMetric).getSpecificLastMetricValue(fieldKey);
    }

    @Override
    public void setSpecificLastMetricValue(HealthMetric healthMetric, String fieldKey, long metricValue) {
        getHealthMetricData(healthMetric).setSpecificLastMetricValue(fieldKey, metricValue);
    }

    public int getDspOutsideNormalCounter() {
        return dspOutsideNormalCounter;
    }

    public HealthState getDspConfirmedState() {
        return dspConfirmedState;
    }

    public HealthState getTccConfirmedState() {
        return tccConfirmedState;
    }

    @Override
    public HealthState getLastTotalHealthState() {
        return lastTotalHealthState;
    }

    @Scheduled(initialDelay = 1000, fixedDelay = 5000)
    public void lastState() {
        if (allowTransactionMonitoring) {
            dspConfirmedState = dspConfirmedStateCheck();
            tccConfirmedState = tccConfirmedStateCheck();
            int logLevel = logLevelCheck(dspConfirmedState, tccConfirmedState);
            printLastState(logLevel);

            updateHealthMetricsSnapshot();
            calculateHealthMetrics();
            calculateTotalHealthState();
        }
    }

    private int logLevelCheck(HealthState dspConfirmedState, HealthState tccConfirmedState) {
        return Math.max(dspConfirmedState.ordinal(), tccConfirmedState.ordinal());
    }

    private HealthState dspConfirmedStateCheck() {
        if (transactionHelper.getTotalTransactions() == confirmationService.getDspConfirmed() ||
                confirmationService.getDspConfirmed() != prevDspConfirmed) {
            prevDspConfirmed = confirmationService.getDspConfirmed();
            dspOutsideNormalCounter = 0;
        } else {
            dspOutsideNormalCounter++;
            if (dspOutsideNormalCounter >= dspThresholdWarning && dspOutsideNormalCounter < dspThresholdError) {
                return HealthState.WARNING;
            } else if (dspOutsideNormalCounter >= dspThresholdError) {
                return HealthState.CRITICAL;
            }
        }
        return HealthState.NORMAL;
    }

    private HealthState tccConfirmedStateCheck() {
        int tccOutsideNormalCounter = trustChainConfirmationService.getTccOutsideNormalCounter();

        if (tccOutsideNormalCounter < tccThresholdWarning) {
            return HealthState.NORMAL;
        }

        return tccOutsideNormalCounter < tccThresholdError ? HealthState.WARNING : HealthState.CRITICAL;
    }

    private void printLastState(int logLevel) {
        String outputText = createOutputAsString(allowTransactionMonitoringDetailed);
        printToLogByLevel(logLevel, outputText);
    }

    private String createOutputAsString(boolean isDetailedLog) {
        StringBuilder output = new StringBuilder();
        if (isDetailedLog) {
            appendOutput(output, "Transactions", transactionHelper.getTotalTransactions());
            appendOutput(output, "TccConfirmed", confirmationService.getTrustChainConfirmed());
            appendOutput(output, "DspConfirmed", confirmationService.getDspConfirmed());
            appendOutput(output, "Confirmed", confirmationService.getTotalConfirmed());
            appendOutput(output, "LastIndex", transactionIndexService.getLastTransactionIndexData().getIndex());
            appendOutput(output, "RejectedTransactions", rejectedTransactions.size());
        }
        appendOutput(output, "Sources", clusterService.getTotalSources());
        appendOutput(output, "DSPHealthState", dspConfirmedState.toString());
        appendOutput(output, "DSPOutsideNormalCounter", dspOutsideNormalCounter);
        appendOutput(output, "TCCHealthState", tccConfirmedState.toString());
        appendOutput(output, "TCCWaitingConfirmation", trustChainConfirmationService.getTccWaitingConfirmation());
        appendOutput(output, "TCCOutsideNormalCounter", trustChainConfirmationService.getTccOutsideNormalCounter());
        appendOutput(output, "PostponedTransactions", transactionService.totalPostponedTransactions());
        appendOutput(output, "PropagationQueue", propagationSubscriber.getMessageQueueSize(ZeroMQSubscriberQueue.TRANSACTION));
        appendOutput(output, "WebSocketMessagesQueueLength", webSocketMessageService.getMessageQueueSize());
        appendOutput(output, "waitingDspConsensus", confirmationService.getWaitingDspConsensusResultsMapSize());
        appendOutput(output, "confirmationQueueSize", confirmationService.getQueueSize());
        appendOutput(output, "percentageUsedHeapMemory", MemoryUtils.getPercentageUsedHeapFormatted());
        appendOutput(output, "percentageUsedMemory", MemoryUtils.getPercentageUsedFormatted());

        return output.toString();
    }

    private void appendOutput(StringBuilder output, String name, long value) {
        output.append(name).append(" = ").append(value).append(", ");
    }

    private void appendOutput(StringBuilder output, String name, int value) {
        output.append(name).append(" = ").append(value).append(", ");
    }

    private void appendOutput(StringBuilder output, String name, String value) {
        output.append(name).append(" = ").append(value).append(", ");
    }

    private void printToLogByLevel(int logLevel, String logText) {
        if (logLevel == 0) {
            log.info(logText);
        } else if (logLevel == 1) {
            log.warn(logText);
        } else if (logLevel == 2) {
            log.error(logText);
        }
    }

    public enum HealthState {
        NORMAL, WARNING, CRITICAL
    }
}
