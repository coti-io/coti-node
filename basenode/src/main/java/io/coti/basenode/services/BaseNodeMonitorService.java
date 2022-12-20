package io.coti.basenode.services;

import io.coti.basenode.communication.interfaces.IPropagationPublisher;
import io.coti.basenode.communication.interfaces.IPropagationSubscriber;
import io.coti.basenode.communication.interfaces.IReceiver;
import io.coti.basenode.data.HealthMetricData;
import io.coti.basenode.data.MetricType;
import io.coti.basenode.database.interfaces.IDatabaseConnector;
import io.coti.basenode.model.RejectedTransactions;
import io.coti.basenode.services.interfaces.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static io.coti.basenode.constants.BaseNodeHealthMetricConstants.DSP_CONFIRMED_LABEL;
import static io.coti.basenode.constants.BaseNodeHealthMetricConstants.TRUST_CHAIN_CONFIRMED_LABEL;

@Slf4j
@Service
public class BaseNodeMonitorService implements IMonitorService {

    private static final String METRIC_TEMPLATE = "metricTemplate";
    private final Map<HealthMetric, HealthMetricData> healthMetrics = new ConcurrentHashMap<>();
    @Autowired
    protected INetworkService networkService;
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
    @Value("${detailed.logs:false}")
    private boolean metricsDetailed;
    @Value("${total.transactions.threshold.warning:1}")
    private int totalTransactionsThresholdWarning;
    @Value("${total.transactions.threshold.critical:2}")
    private int totalTransactionsThresholdCritical;
    @Value("${sources.upperBound.threshold.warning:24}")
    private int sourcesUpperBoundThresholdWarning;
    @Value("${sources.upperBound.threshold.critical:34}")
    private int sourcesUpperBoundThresholdCritical;
    @Value("${sources.lowerBound.threshold.warning:-8}")
    private int sourcesLowerBoundThresholdWarning;
    @Value("${sources.lowerBound.threshold.critical:-6}")
    private int sourcesLowerBoundThresholdCritical;
    @Value("${index.threshold.warning:2}")
    private int indexThresholdWarning;
    @Value("${index.threshold.critical:0}")
    private int indexThresholdCritical;
    @Value("${waiting.dspConsensus.threshold.warning:1}")
    private int waitingDSPConsensusThresholdWarning;
    @Value("${waiting.dspConsensus.threshold.critical:5}")
    private int waitingDSPConsensusThresholdCritical;
    @Value("${dsp.outsideNormal.threshold.warning:2}")
    private int dspOutsideNormalThresholdWarning;
    @Value("${dsp.outsideNormal.threshold.critical:5}")
    private int dspOutsideNormalThresholdCritical;
    @Value("${totalConfirmed.outsideNormal.threshold.warning:2}")
    private int totalConfirmedOutsideNormalThresholdWarning;
    @Value("${totalConfirmed.outsideNormal.threshold.critical:5}")
    private int totalConfirmedOutsideNormalThresholdCritical;
    @Value("${tcc.outsideNormal.threshold.warning:5}")
    private int tccOutsideNormalThresholdWarning;
    @Value("${tcc.outsideNormal.threshold.critical:10}")
    private int tccOutsideNormalThresholdCritical;
    @Value("${waiting.missingTransactionsIndexes.threshold.warning:1}")
    private int waitingMissingTransactionsIndexesThresholdWarning;
    @Value("${waiting.missingTransactionsIndexes.threshold.critical:1}")
    private int waitingMissingTransactionsIndexesThresholdCritical;
    @Value("${total.postponedTransactions.threshold.warning:2}")
    private int totalPostponedTransactionsIndexesThresholdWarning;
    @Value("${total.postponedTransactions.threshold.critical:4}")
    private int totalPostponedTransactionsIndexesThresholdCritical;
    @Value("${propagation.queue.threshold.warning:64}")
    private int propagationQueueThresholdWarning;
    @Value("${propagation.queue.threshold.critical:0}")
    private int propagationQueueThresholdCritical;
    @Value("${webSocketMessages.queueLengthQueue.threshold.warning:100}")
    private int webSocketMessagesQueueLengthWarning;
    @Value("${webSocketMessages.queueLengthQueue.threshold.critical:1000}")
    private int webSocketMessagesQueueLengthCritical;
    @Value("${confirmation.queueSize.threshold.warning:100}")
    private int dcrConfirmationQueueSizeWarning;
    @Value("${confirmation.queueSize.threshold.critical:0}")
    private int dcrConfirmationQueueSizeCritical;
    @Value("${tcc.confirmation.queueSize.threshold.warning:100}")
    private int tccConfirmationQueueSizeWarning;
    @Value("${tcc.confirmation.queueSize.threshold.critical:0}")
    private int tccConfirmationQueueSizeCritical;
    @Value("${percentage.usedHeapMemory.threshold.warning:95}")
    private int percentageUsedHeapMemoryWarning;
    @Value("${percentage.usedHeapMemory.threshold.critical:98}")
    private int percentageUsedHeapMemoryCritical;
    @Value("${percentage.usedMemory.threshold.warning:85}")
    private int percentageUsedMemoryWarning;
    @Value("${percentage.usedMemory.threshold.critical:95}")
    private int percentageUsedMemoryCritical;
    @Value("${connectedToRecovery.threshold.warning:1}")
    private int connectedToRecoveryThresholdWarning;
    @Value("${connectedToRecovery.threshold.critical:1}")
    private int connectedToRecoveryThresholdCritical;
    @Value("${propSub.transactionsState.queue.threshold.warning:5}")
    private int propSubTransactionsStateQueueThresholdWarning;
    @Value("${propSub.transactionsState.queue.threshold.critical:0}")
    private int propSubTransactionsStateQueueThresholdCritical;
    @Value("${propSub.network.queue.threshold.warning:10}")
    private int propSubNetworkQueueThresholdWarning;
    @Value("${propSub.network.queue.threshold.critical:0}")
    private int propSubNetworkQueueThresholdCritical;
    @Value("${propSub.address.queue.threshold.warning:40}")
    private int propSubAddressQueueThresholdWarning;
    @Value("${propSub.address.queue.threshold.critical:0}")
    private int propSubAddressQueueThresholdCritical;
    @Value("${propSub.transaction.queue.threshold.warning:100}")
    private int propSubTransactionQueueThresholdWarning;
    @Value("${propSub.transaction.queue.threshold.critical:0}")
    private int propSubTransactionQueueThresholdCritical;
    @Value("${propSub.heartbeat.queue.threshold.warning:10}")
    private int propSubHeartbeatQueueThresholdWarning;
    @Value("${propSub.heartbeat.queue.threshold.critical:0}")
    private int propSubHeartbeatQueueThresholdCritical;
    @Value("${zeroMQReceiver.queue.threshold.warning:100}")
    private int zeroMQReceiverQueueThresholdWarning;
    @Value("${zeroMQReceiver.queue.threshold.critical:0}")
    private int zeroMQReceiverQueueThresholdCritical;
    @Value("${propagationPublisher.queue.threshold.warning:100}")
    private int propagationPublisherQueueThresholdWarning;
    @Value("${propagationPublisher.queue.threshold.critical:0}")
    private int propagationPublisherQueueThresholdCritical;
    @Value("${liveFilesSize.threshold.warning:100}")
    private int liveFilesSizeThresholdWarning;
    @Value("${liveFilesSize.threshold.critical:0}")
    private int liveFilesSizeThresholdCritical;
    @Value("${backupHourly.threshold.warning:2400}")
    private int backupHourlyThresholdWarning;
    @Value("${backupHourly.threshold.critical:4800}")
    private int backupHourlyThresholdCritical;
    @Value("${backupEpoch.threshold.warning:3600}")
    private int backupEpochThresholdWarning;
    @Value("${backupEpoch.threshold.critical:0}")
    private int backupEpochThresholdCritical;
    @Value("${backupNumberOfFiles.threshold.warning:1}")
    private int backupNumberOfFilesThresholdWarning;
    @Value("${backupNumberOfFiles.threshold.critical:1}")
    private int backupNumberOfFilesThresholdCritical;
    @Value("${backupSize.threshold.warning:0}")
    private int backupSizeThresholdWarning;
    @Value("${backupSize.threshold.critical:0}")
    private int backupSizeThresholdCritical;
    @Value("${backupEntireDuration.threshold.warning:75}")
    private int backupEntireDurationThresholdWarning;
    @Value("${backupEntireDuration.threshold.critical:180}")
    private int backupEntireDurationThresholdCritical;
    @Value("${backupDuration.threshold.warning:45}")
    private int backupDurationThresholdWarning;
    @Value("${backupDuration.threshold.critical:90}")
    private int backupDurationThresholdCritical;
    @Value("${backupUploadDuration.threshold.warning:20}")
    private int backupUploadDurationThresholdWarning;
    @Value("${backupUploadDuration.threshold.critical:60}")
    private int backupUploadDurationThresholdCritical;
    @Value("${backupRemovalDuration.threshold.warning:10}")
    private int backupRemovalDurationThresholdWarning;
    @Value("${backupRemovalDuration.threshold.critical:30}")
    private int backupRemovalDurationThresholdCritical;
    @Value("${rejected.transactions.threshold.warning:10}")
    private int rejectedTransactionsThresholdWarning;
    @Value("${rejected.transactions.threshold.critical:0}")
    private int rejectedTransactionsThresholdCritical;
    private HealthState lastTotalHealthState = HealthState.NA;

    public void init() {
        log.info("{} is up", this.getClass().getSimpleName());
    }

    @PostConstruct
    private void initHealthMetrics() {
        for (HealthMetric hm : HealthMetric.values()) {
            hm.monitorService = this;
        }
        HealthMetric.TOTAL_TRANSACTIONS.transactionHelper = transactionHelper;

        HealthMetric.SOURCES_UPPER_BOUND.clusterService = clusterService;

        HealthMetric.SOURCES_LOWER_BOUND.clusterService = clusterService;

        HealthMetric.INDEX.transactionHelper = transactionHelper;
        HealthMetric.INDEX.transactionIndexService = transactionIndexService;

        HealthMetric.WAITING_DSP_CONSENSUS_RESULTS_CONFIRMED.confirmationService = confirmationService;

        HealthMetric.DSP_CONFIRMED.confirmationService = confirmationService;
        HealthMetric.DSP_CONFIRMED.transactionHelper = transactionHelper;

        HealthMetric.TOTAL_CONFIRMED.confirmationService = confirmationService;

        HealthMetric.TRUST_CHAIN_CONFIRMED.trustChainConfirmationService = trustChainConfirmationService;
        HealthMetric.TRUST_CHAIN_CONFIRMED.confirmationService = confirmationService;

        HealthMetric.WAITING_MISSING_TRANSACTION_INDEXES.confirmationService = confirmationService;

        HealthMetric.TOTAL_POSTPONED_TRANSACTIONS.transactionService = transactionService;

        HealthMetric.PROPAGATION_QUEUE.propagationSubscriber = propagationSubscriber;

        HealthMetric.WEB_SOCKET_MESSAGES_QUEUE_LENGTH.webSocketMessageService = webSocketMessageService;

        HealthMetric.DCR_CONFIRMATION_QUEUE_SIZE.confirmationService = confirmationService;

        HealthMetric.TCC_CONFIRMATION_QUEUE_SIZE.confirmationService = confirmationService;

        HealthMetric.CONNECTED_TO_RECOVERY.networkService = networkService;

        HealthMetric.PROPAGATION_SUBSCRIBER_TRANSACTIONS_STATE_QUEUE_SIZE.propagationSubscriber = propagationSubscriber;

        HealthMetric.PROPAGATION_SUBSCRIBER_NETWORK_QUEUE_SIZE.propagationSubscriber = propagationSubscriber;

        HealthMetric.PROPAGATION_SUBSCRIBER_ADDRESS_QUEUE_SIZE.propagationSubscriber = propagationSubscriber;

        HealthMetric.PROPAGATION_SUBSCRIBER_TRANSACTION_QUEUE_SIZE.propagationSubscriber = propagationSubscriber;

        HealthMetric.PROPAGATION_SUBSCRIBER_HEARTBEAT_QUEUE_SIZE.propagationSubscriber = propagationSubscriber;

        HealthMetric.ZERO_MQ_RECEIVER_QUEUE_SIZE.receiver = receiver;

        HealthMetric.PROPAGATION_PUBLISHER_QUEUE_SIZE.propagationPublisher = propagationPublisher;

        HealthMetric.LIVE_FILES_SIZE.databaseConnector = databaseConnector;

        HealthMetric.BACKUP_HOURLY.dbRecoveryService = dbRecoveryService;

        HealthMetric.BACKUP_EPOCH.dbRecoveryService = dbRecoveryService;

        HealthMetric.BACKUP_NUMBER_OF_FILES.dbRecoveryService = dbRecoveryService;
        HealthMetric.BACKUP_NUMBER_OF_FILES.databaseConnector = databaseConnector;

        HealthMetric.BACKUP_SIZE.dbRecoveryService = dbRecoveryService;

        HealthMetric.BACKUP_ENTIRE_DURATION.dbRecoveryService = dbRecoveryService;

        HealthMetric.BACKUP_DURATION.dbRecoveryService = dbRecoveryService;

        HealthMetric.BACKUP_UPLOAD_DURATION.dbRecoveryService = dbRecoveryService;

        HealthMetric.BACKUP_REMOVAL_DURATION.dbRecoveryService = dbRecoveryService;

        HealthMetric.REJECTED_TRANSACTIONS.rejectedTransactions = rejectedTransactions;

        HealthMetric.TOTAL_TRANSACTIONS.setThresholds(totalTransactionsThresholdWarning, totalTransactionsThresholdCritical);
        HealthMetric.SOURCES_UPPER_BOUND.setThresholds(sourcesUpperBoundThresholdWarning, sourcesUpperBoundThresholdCritical);
        HealthMetric.SOURCES_LOWER_BOUND.setThresholds(sourcesLowerBoundThresholdWarning, sourcesLowerBoundThresholdCritical);
        HealthMetric.INDEX.setThresholds(indexThresholdWarning, indexThresholdCritical);
        HealthMetric.WAITING_DSP_CONSENSUS_RESULTS_CONFIRMED.setThresholds(waitingDSPConsensusThresholdWarning, waitingDSPConsensusThresholdCritical);
        HealthMetric.WAITING_MISSING_TRANSACTION_INDEXES.setThresholds(waitingMissingTransactionsIndexesThresholdWarning, waitingMissingTransactionsIndexesThresholdCritical);
        HealthMetric.DSP_CONFIRMED.setThresholds(dspOutsideNormalThresholdWarning, dspOutsideNormalThresholdCritical);
        HealthMetric.TOTAL_CONFIRMED.setThresholds(totalConfirmedOutsideNormalThresholdWarning, totalConfirmedOutsideNormalThresholdCritical);
        HealthMetric.TRUST_CHAIN_CONFIRMED.setThresholds(tccOutsideNormalThresholdWarning, tccOutsideNormalThresholdCritical);
        HealthMetric.TOTAL_POSTPONED_TRANSACTIONS.setThresholds(totalPostponedTransactionsIndexesThresholdWarning, totalPostponedTransactionsIndexesThresholdCritical);
        HealthMetric.PROPAGATION_QUEUE.setThresholds(propagationQueueThresholdWarning, propagationQueueThresholdCritical);
        HealthMetric.WEB_SOCKET_MESSAGES_QUEUE_LENGTH.setThresholds(webSocketMessagesQueueLengthWarning, webSocketMessagesQueueLengthCritical);
        HealthMetric.DCR_CONFIRMATION_QUEUE_SIZE.setThresholds(dcrConfirmationQueueSizeWarning, dcrConfirmationQueueSizeCritical);
        HealthMetric.TCC_CONFIRMATION_QUEUE_SIZE.setThresholds(tccConfirmationQueueSizeWarning, tccConfirmationQueueSizeCritical);
        HealthMetric.PERCENTAGE_USED_HEAP_MEMORY.setThresholds(percentageUsedHeapMemoryWarning, percentageUsedHeapMemoryCritical);
        HealthMetric.PERCENTAGE_USED_MEMORY.setThresholds(percentageUsedMemoryWarning, percentageUsedMemoryCritical);
        HealthMetric.CONNECTED_TO_RECOVERY.setThresholds(connectedToRecoveryThresholdWarning, connectedToRecoveryThresholdCritical);

        HealthMetric.PROPAGATION_SUBSCRIBER_TRANSACTIONS_STATE_QUEUE_SIZE.setThresholds(propSubTransactionsStateQueueThresholdWarning, propSubTransactionsStateQueueThresholdCritical);
        HealthMetric.PROPAGATION_SUBSCRIBER_NETWORK_QUEUE_SIZE.setThresholds(propSubNetworkQueueThresholdWarning, propSubNetworkQueueThresholdCritical);
        HealthMetric.PROPAGATION_SUBSCRIBER_ADDRESS_QUEUE_SIZE.setThresholds(propSubAddressQueueThresholdWarning, propSubAddressQueueThresholdCritical);
        HealthMetric.PROPAGATION_SUBSCRIBER_TRANSACTION_QUEUE_SIZE.setThresholds(propSubTransactionQueueThresholdWarning, propSubTransactionQueueThresholdCritical);
        HealthMetric.PROPAGATION_SUBSCRIBER_HEARTBEAT_QUEUE_SIZE.setThresholds(propSubHeartbeatQueueThresholdWarning, propSubHeartbeatQueueThresholdCritical);
        HealthMetric.ZERO_MQ_RECEIVER_QUEUE_SIZE.setThresholds(zeroMQReceiverQueueThresholdWarning, zeroMQReceiverQueueThresholdCritical);
        HealthMetric.PROPAGATION_PUBLISHER_QUEUE_SIZE.setThresholds(propagationPublisherQueueThresholdWarning, propagationPublisherQueueThresholdCritical);
        HealthMetric.LIVE_FILES_SIZE.setThresholds(liveFilesSizeThresholdWarning, liveFilesSizeThresholdCritical);

        HealthMetric.BACKUP_HOURLY.setThresholds(backupHourlyThresholdWarning, backupHourlyThresholdCritical);
        HealthMetric.BACKUP_EPOCH.setThresholds(backupEpochThresholdWarning, backupEpochThresholdCritical);
        HealthMetric.BACKUP_NUMBER_OF_FILES.setThresholds(backupNumberOfFilesThresholdWarning, backupNumberOfFilesThresholdCritical);
        HealthMetric.BACKUP_SIZE.setThresholds(backupSizeThresholdWarning, backupSizeThresholdCritical);
        HealthMetric.BACKUP_ENTIRE_DURATION.setThresholds(backupEntireDurationThresholdWarning, backupEntireDurationThresholdCritical);
        HealthMetric.BACKUP_DURATION.setThresholds(backupDurationThresholdWarning, backupDurationThresholdCritical);
        HealthMetric.BACKUP_UPLOAD_DURATION.setThresholds(backupUploadDurationThresholdWarning, backupUploadDurationThresholdCritical);
        HealthMetric.BACKUP_REMOVAL_DURATION.setThresholds(backupRemovalDurationThresholdWarning, backupRemovalDurationThresholdCritical);

        HealthMetric.REJECTED_TRANSACTIONS.setThresholds(rejectedTransactionsThresholdWarning, rejectedTransactionsThresholdCritical);

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
        for (Map.Entry<HealthMetric, HealthMetricData> entry : getHealthMetrics().entrySet()) {
            HealthMetricData metricData = entry.getValue();
            if (metricData.getLastHealthState().ordinal() > calculatedTotalHealthState.ordinal()) {
                calculatedTotalHealthState = metricData.getLastHealthState();
            }
        }
        this.lastTotalHealthState = calculatedTotalHealthState;
    }

    private String createHealthStateOutputAsString(StringBuilder output) {
        appendOutput(output, " TotalHealthState ", getLastTotalHealthState().toString());
        if (getLastTotalHealthState().ordinal() > HealthState.NORMAL.ordinal()) {
            for (Map.Entry<HealthMetric, HealthMetricData> entry : healthMetrics.entrySet()) {
                HealthMetricData metricData = entry.getValue();
                HealthMetric healthMetric = entry.getKey();
                if (metricData.getLastHealthState() != HealthState.NORMAL && metricData.getLastHealthState() != HealthState.NA) {
                    output.append(healthMetric.label).append(" state = ").append(metricData.getLastHealthState().toString());
                    if (healthMetric.isCounterBased()) {
                        output.append(", counter = ").append(metricData.getLastCounter()).append(", ");
                    } else {
                        output.append(", value = ").append(metricData.getLastMetricValue()).append(", ");
                    }
                }
            }
        }
        return output.toString();
    }

    @Override
    public HealthMetricData getHealthMetricData(HealthMetric healthMetric) {
        return Optional.ofNullable(healthMetrics.get(healthMetric)).orElseThrow(() -> new IllegalArgumentException("No matching health metric found"));
    }

    @Override
    public HealthMetricData getHealthMetricData(String label) {
        return getHealthMetrics().get(HealthMetric.getHealthMetric(label));
    }

    public HealthMetric getHealthMetric(String label) {
        return HealthMetric.getHealthMetric(label);
    }

    @Override
    public Map<HealthMetric, HealthMetricData> getHealthMetrics() {
        return healthMetrics;
    }

    @Override
    public void setLastMetricValue(HealthMetric healthMetric, long metricValue) {
        getHealthMetricData(healthMetric).setLastMetricValue(metricValue);
    }

    @Override
    public void setSnapshotTime(HealthMetric healthMetric, String snapshotTime) {
        getHealthMetricData(healthMetric).setSnapshotTime(snapshotTime);
    }

    @Override
    public long getSpecificLastMetricValue(HealthMetric healthMetric, String fieldKey) {
        return getHealthMetricData(healthMetric).getSpecificLastMetricValue(fieldKey);
    }

    @Override
    public void setSpecificLastMetricValue(HealthMetric healthMetric, String fieldKey, long metricValue) {
        getHealthMetricData(healthMetric).setSpecificLastMetricValue(fieldKey, metricValue);
    }

    @Override
    public void updateHealthMetrics(ArrayList<String> metrics, HashMap<String, String> metricTemplateMap) {
        for (HealthMetric healthMetric : HealthMetric.values()) {
            addMetric(healthMetric.label, metrics, metricTemplateMap);
        }
    }

    private void addMetric(String healthMetricLabel, ArrayList<String> metrics, HashMap<String, String> metricTemplateMap) {
        HealthMetric healthMetric = HealthMetric.getHealthMetric(healthMetricLabel);
        HealthMetricData healthMetricData = getHealthMetrics().get(healthMetric);
        String snapshotTime = healthMetricData.getSnapshotTime();
        MetricType metricType = healthMetric.getMetricType();
        if (metricType == MetricType.NA) {
            return;
        }
        String metricTemplateVal = metricTemplateMap.get(metricType.name());
        if (metricsDetailed || !healthMetric.isDetailedLogs()) {
            addMetric(metrics, metricTemplateVal, metricType, healthMetricLabel, healthMetricData.getLastMetricValue(), snapshotTime);
        }
        addMetric(metrics, metricTemplateVal, metricType, healthMetricLabel + "State", healthMetricData.getLastHealthState().ordinal(), snapshotTime);
    }

    private void addMetric(ArrayList<String> metrics, String metricTemplate, MetricType metricType, String healthMetricLabel, long lastMetricValue, String snapshotTime) {
        if (metricType == MetricType.BACKUP_METRIC) {
            String s3FolderName = dbRecoveryService.getS3FolderName();
            if (s3FolderName != null) {
                metrics.add(metricTemplate.replace(METRIC_TEMPLATE, healthMetricLabel).replace("componentNameTemplate", s3FolderName)
                        .concat(" ").concat(String.valueOf(lastMetricValue)).concat(" ").concat(snapshotTime));
            }
        } else {
            metrics.add(metricTemplate.replace(METRIC_TEMPLATE, healthMetricLabel)
                    .concat(" ").concat(String.valueOf(lastMetricValue)).concat(" ").concat(snapshotTime));
        }
    }

    @Override
    public Health getHealthBuilder(String label) {
        Health.Builder builder = new Health.Builder();
        HealthMetricData healthMetricData = getHealthMetricData(label);
        if (healthMetricData.getLastHealthState().ordinal() == BaseNodeMonitorService.HealthState.NA.ordinal()) {
            builder.unknown();
            return builder.build();
        }

        if (healthMetricData.getLastHealthState().ordinal() == BaseNodeMonitorService.HealthState.CRITICAL.ordinal()) {
            builder.down();
        } else {
            builder.up();
        }
        builder.withDetail("State", healthMetricData.getLastHealthState());
        HealthMetric healthMetric = getHealthMetric(label);
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
            builder.withDetail("ConditionResultValue", healthMetricData.getLastConditionValue());
            builder.withDetail("SnapshotValue", healthMetricData.getLastMetricValue());
            if (healthMetric.isCounterBased()) {
                builder.withDetail("Counter", healthMetricData.getLastCounter());
            }
            healthMetricData.getAdditionalValues().forEach((key, value) -> builder.withDetail(key, value.toString()));
        }
        builder.withDetail("Configuration", configMap);
        return builder.build();
    }

    @Override
    public HealthState getLastTotalHealthState() {
        return lastTotalHealthState;
    }

    @Scheduled(initialDelay = 1000, fixedDelay = 5000)
    public void lastState() {
        if (allowTransactionMonitoring) {
            updateHealthMetricsSnapshot();
            calculateHealthMetrics();
            calculateTotalHealthState();

            int logLevel = lastTotalHealthState.ordinal();
            printLastState(logLevel);
        }
    }

    private void printLastState(int logLevel) {
        String outputText = createOutputAsString(allowTransactionMonitoringDetailed);
        printToLogByLevel(logLevel, outputText);
    }

    private String createOutputAsString(boolean isDetailedLog) {
        StringBuilder output = new StringBuilder();
        if (isDetailedLog) {
            appendOutput(output, HealthMetric.TOTAL_TRANSACTIONS);
            appendOutput(output, HealthMetric.TRUST_CHAIN_CONFIRMED);
            appendOutput(output, HealthMetric.DSP_CONFIRMED);
            appendOutput(output, HealthMetric.TOTAL_CONFIRMED);
            appendOutput(output, HealthMetric.INDEX);
            appendOutput(output, HealthMetric.REJECTED_TRANSACTIONS);
        }
        appendOutput(output, HealthMetric.SOURCES_UPPER_BOUND);
        appendOutput(output, HealthMetric.SOURCES_LOWER_BOUND);

        appendOutput(output, "DSPHealthState", getHealthMetricData(DSP_CONFIRMED_LABEL).getLastHealthState().toString());
        appendOutput(output, "TCCHealthState", getHealthMetricData(TRUST_CHAIN_CONFIRMED_LABEL).getLastHealthState().toString());

        appendOutput(output, HealthMetric.TOTAL_POSTPONED_TRANSACTIONS);
        appendOutput(output, HealthMetric.PROPAGATION_QUEUE);
        appendOutput(output, HealthMetric.WEB_SOCKET_MESSAGES_QUEUE_LENGTH);
        appendOutput(output, HealthMetric.WAITING_DSP_CONSENSUS_RESULTS_CONFIRMED);
        appendOutput(output, HealthMetric.DCR_CONFIRMATION_QUEUE_SIZE);
        appendOutput(output, HealthMetric.TCC_CONFIRMATION_QUEUE_SIZE);
        appendOutput(output, HealthMetric.PERCENTAGE_USED_HEAP_MEMORY);
        appendOutput(output, HealthMetric.PERCENTAGE_USED_MEMORY);
        appendOutput(output, HealthMetric.CONNECTED_TO_RECOVERY);

        return createHealthStateOutputAsString(output);
    }

    private void appendOutput(StringBuilder output, HealthMetric healthMetric) {
        appendOutput(output, healthMetric.label, healthMetric.getHealthMetricData().getLastMetricValue());
    }

    private void appendOutput(StringBuilder output, String name, long value) {
        output.append(name).append(" = ").append(value).append(", ");
    }

    private void appendOutput(StringBuilder output, String name, String value) {
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
