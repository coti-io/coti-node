package io.coti.basenode.services;

import io.coti.basenode.data.HealthMetricData;
import io.coti.basenode.communication.interfaces.IPropagationPublisher;
import io.coti.basenode.communication.interfaces.IPropagationSubscriber;
import io.coti.basenode.communication.interfaces.IReceiver;
import io.coti.basenode.database.interfaces.IDatabaseConnector;
import io.coti.basenode.model.RejectedTransactions;
import io.coti.basenode.services.interfaces.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static io.coti.basenode.http.BaseNodeHealthMetricConstants.*;

@Slf4j
@Service
public class BaseNodeMetricsService implements IMetricsService {

    private static final int MAX_NUMBER_OF_NON_FETCHED_SAMPLES = 50;
    private static final String COMPONENT_TEMPLATE = "componentTemplate";
    private static final String METRIC_TEMPLATE = "metricTemplate";
    private final ArrayList<String> metrics = new ArrayList<>();
    private final AtomicInteger numberOfNonFetchedSamples = new AtomicInteger(0);
    private String metricTemplate = "coti_node{host=\"nodeTemplate\",components=\"componentTemplate\",metric=\"metricTemplate\"}";
    private String metricTemplateSubComponent = "coti_node{host=\"nodeTemplate\",components=\"componentTemplate\",componentName=\"componentNameTemplate\",metric=\"metricTemplate\"}";
    private String metricQueuesTemplate;
    private String metricTransactionsTemplate;
    private String metricBackupsTemplate;
    private String metricDatabaseTemplate;
    private Thread sampleThread;
    @Autowired
    private BaseNodeMonitorService baseNodeMonitorService;
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
    private IDBRecoveryService dbRecoveryService;

    @Value("${metrics.sample.milisec.interval:0}")
    private int metricsSampleInterval;
    @Value("${detailed.logs:false}")
    private boolean metricsDetailed;

    public void init() {
        if (metricsSampleInterval == 0) {
            log.info("Not using metrics endpoint, {} initialization stopped...", this.getClass().getSimpleName());
            return;
        }
        if (metricsSampleInterval < 1000) {
            log.error("Metrics samples are too low (minimum 1000), {} initialization stopped...", this.getClass().getSimpleName());
            metricsSampleInterval = 0;
            return;
        }
        String hostName = "unknown";
        try {
            hostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            log.error(e.toString());
        }
        metricTemplate = metricTemplate.replace("nodeTemplate", hostName);
        metricTemplateSubComponent = metricTemplateSubComponent.replace("nodeTemplate", hostName);

        metricQueuesTemplate = metricTemplate.replace(COMPONENT_TEMPLATE, "queues");
        metricTransactionsTemplate = metricTemplate.replace(COMPONENT_TEMPLATE, "transactions");
        metricBackupsTemplate = metricTemplateSubComponent.replace(COMPONENT_TEMPLATE, "backups");
        metricDatabaseTemplate = metricTemplate.replace(COMPONENT_TEMPLATE, "database");

        sampleThread = new Thread(this::getMetricsSample, "MetricsSample");
        sampleThread.start();
        log.info("{} is up", this.getClass().getSimpleName());
    }


    @Override
    public String getMetrics(HttpServletRequest request) {
        if (sampleThread == null || !sampleThread.isAlive()) {
            log.error("Metrics sample thread not initialized!, returning null to {}...", request.getRemoteAddr());
            return null;
        }
        synchronized (metrics) {
            numberOfNonFetchedSamples.set(0);
            String val = String.join("\n", metrics).concat("\n");
            metrics.clear();
            return val;
        }
    }

    private void addQueue(String queueMetric, long value, String snapshotTime) {
        metrics.add(metricQueuesTemplate.replace(METRIC_TEMPLATE, queueMetric)
                .concat(" ").concat(String.valueOf(value)).concat(" ").concat(snapshotTime));
    }

    private void addQueue(String healthMetricLabel) {
        HealthMetricData healthMetricData = baseNodeMonitorService.getHealthMetrics().get(HealthMetric.getHealthMetric(healthMetricLabel));
        String snapshotTime = healthMetricData.getSnapshotTime();
        addQueue(healthMetricLabel, healthMetricData.getLastMetricValue(), snapshotTime);
        addQueue(healthMetricLabel + "State", healthMetricData.getLastHealthState().ordinal(), snapshotTime);
    }

    private void addTransaction(String transactionMetric, long value, String snapshotTime) {
        metrics.add(metricTransactionsTemplate.replace(METRIC_TEMPLATE, transactionMetric)
                .concat(" ").concat(String.valueOf(value)).concat(" ").concat(snapshotTime));
    }

    private void addTransaction(String healthMetricLabel) {
        HealthMetric healthMetric = HealthMetric.getHealthMetric(healthMetricLabel);
        HealthMetricData healthMetricData = baseNodeMonitorService.getHealthMetrics().get(healthMetric);
        String snapshotTime = healthMetricData.getSnapshotTime();
        if (metricsDetailed || !healthMetric.isDetailedLogs()) {
            addTransaction(healthMetricLabel, healthMetricData.getLastMetricValue(), snapshotTime);
        }
        addTransaction(healthMetricLabel + "State", healthMetricData.getLastHealthState().ordinal(), snapshotTime);
    }

    private void addBackup(String backupMetric, String backupName, long value, String snapshotTime) {
        metrics.add(metricBackupsTemplate.replace(METRIC_TEMPLATE, backupMetric).replace("componentNameTemplate", backupName)
                .concat(" ").concat(String.valueOf(value)).concat(" ").concat(snapshotTime));
    }

    private void addBackup(String healthMetricLabel) {
        HealthMetric healthMetric = HealthMetric.getHealthMetric(healthMetricLabel);
        HealthMetricData healthMetricData = baseNodeMonitorService.getHealthMetrics().get(healthMetric);
        String snapshotTime = healthMetricData.getSnapshotTime();
        if (healthMetric.getComponentTemplate().equals(COMPONENT_TEMPLATE_BACKUPS)) {
            String s3FolderName = dbRecoveryService.getS3FolderName();
            if (s3FolderName != null) {
                addBackup(healthMetricLabel, s3FolderName, healthMetricData.getLastMetricValue(), snapshotTime);
            }
        }
    }

    private void addDatabase(String databaseMetric, long value, String snapshotTime) {
        metrics.add(metricDatabaseTemplate.replace(METRIC_TEMPLATE, databaseMetric)
                .concat(" ").concat(String.valueOf(value)).concat(" ").concat(snapshotTime));
    }

    private void addDatabase(String healthMetricLabel) {
        HealthMetricData healthMetricData = baseNodeMonitorService.getHealthMetrics().get(HealthMetric.getHealthMetric(healthMetricLabel));
        String snapshotTime = healthMetricData.getSnapshotTime();
        addDatabase(healthMetricLabel, healthMetricData.getLastMetricValue(), snapshotTime);
    }

    private void addBackups() {
        HashMap<String, HashMap<String, Long>> backupLog = dbRecoveryService.getBackUpLog();
        addBackup(BACKUP_HOURLY_LABEL);
        addBackup(BACKUP_EPOCH_LABEL);
        addBackup(BACKUP_NUMBER_OF_FILES_LABEL);
        addBackup(BACKUP_SIZE_LABEL);
        addBackup(BACKUP_ENTIRE_DURATION_LABEL);
        addBackup(BACKUP_DURATION_LABEL);
        addBackup(BACKUP_UPLOAD_DURATION_LABEL);
        addBackup(BACKUP_REMOVAL_DURATION_LABEL);
        if (backupLog.size() > 0) {
            dbRecoveryService.clearBackupLog();
        }
    }

    public void getMetricsSample() {
        while (!Thread.currentThread().isInterrupted()) {
            synchronized (metrics) {
                if (numberOfNonFetchedSamples.incrementAndGet() > MAX_NUMBER_OF_NON_FETCHED_SAMPLES) {
                    metrics.clear();
                }

                addQueue(ZERO_MQ_RECEIVER_QUEUE_SIZE_LABEL);
                addQueue(PROPAGATION_PUBLISHER_QUEUE_SIZE_LABEL);
                addQueue(PROPAGATION_SUBSCRIBER_TRANSACTIONS_STATE_QUEUE_SIZE_LABEL);
                addQueue(PROPAGATION_SUBSCRIBER_NETWORK_QUEUE_SIZE_LABEL);
                addQueue(PROPAGATION_SUBSCRIBER_ADDRESS_QUEUE_SIZE_LABEL);
                addQueue(PROPAGATION_SUBSCRIBER_TRANSACTION_QUEUE_SIZE_LABEL);
                addQueue(PROPAGATION_SUBSCRIBER_HEARTBEAT_QUEUE_SIZE_LABEL);
                addQueue(CONFIRMATION_QUEUE_SIZE_LABEL);
                addQueue(WEB_SOCKET_MESSAGES_QUEUE_LENGTH_LABEL);

                if (metricsDetailed) {
                    addTransaction(TOTAL_TRANSACTIONS_LABEL);
                    addTransaction(TRUST_CHAIN_CONFIRMED_LABEL);
                    addTransaction(DSP_CONFIRMED_LABEL);
                    addTransaction(TOTAL_CONFIRMED_LABEL);
                    addTransaction(INDEX_LABEL);
//                    addTransaction("RejectedTransactions", rejectedTransactions.size());
                }
                addTransaction(WAITING_DSP_CONSENSUS_RESULTS_CONFIRMED_LABEL);
                addTransaction(WAITING_MISSING_TRANSACTION_INDEXES_LABEL);
                addTransaction(SOURCES_UPPER_BOUND_LABEL);
                addTransaction(SOURCES_LOWER_BOUND_LABEL);
                addTransaction(TOTAL_POSTPONED_TRANSACTIONS_LABEL);
                addTransaction(CONNECTED_TO_RECOVERY_LABEL);

                addDatabase(LIVE_FILES_SIZE_LABEL);

                addBackups();
            }
            try {
                Thread.sleep(metricsSampleInterval);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
