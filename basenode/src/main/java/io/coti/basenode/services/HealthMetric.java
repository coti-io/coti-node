package io.coti.basenode.services;

import io.coti.basenode.communication.ZeroMQSubscriberQueue;
import io.coti.basenode.communication.interfaces.IPropagationPublisher;
import io.coti.basenode.communication.interfaces.IPropagationSubscriber;
import io.coti.basenode.communication.interfaces.IReceiver;
import io.coti.basenode.data.HealthMetricData;
import io.coti.basenode.data.HealthMetricOutputType;
import io.coti.basenode.data.MetricClass;
import io.coti.basenode.database.interfaces.IDatabaseConnector;
import io.coti.basenode.services.interfaces.*;
import io.coti.basenode.utilities.MemoryUtils;
import org.springframework.boot.actuate.health.Health;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static io.coti.basenode.constants.BaseNodeHealthMetricConstants.*;


public enum HealthMetric implements IHealthMetric {

    TOTAL_TRANSACTIONS_DELTA(TOTAL_TRANSACTIONS_DELTA_LABEL, true, MetricClass.TRANSACTIONS_METRIC, 0, 0, true, HealthMetricOutputType.INFLUX) {
        @Override
        public void doSnapshot() {
            long totalTransactions = transactionHelper.getTotalTransactions();
            HealthMetricData healthMetricData = this.getHealthMetricData();
            healthMetricData.addValue("Transactions", HealthMetricOutputType.ALL, "Transactions", totalTransactions);
            long totalTransactionsFromRecoveryServer = transactionHelper.getTotalNumberOfTransactionsFromRecovery();
            if (totalTransactionsFromRecoveryServer > 0) {
                healthMetricData.addValue("TotalNumberOfTransactionsFromRecovery", HealthMetricOutputType.INFLUX, "TotalNumberOfTransactionsFromRecovery", totalTransactionsFromRecoveryServer);
                baseDoSnapshot(this, totalTransactions - totalTransactionsFromRecoveryServer);
            } else {
                baseDoSnapshot(this, (long) -1);
            }
        }

        @Override
        public void calculateHealthMetric() {
            if (monitorService.getHealthMetricData(this).getMetricValue() > -1) {
                baseCalculateHealthCounterMetricState(this);
            } else {
                monitorService.getHealthMetricData(this).setLastHealthState(BaseNodeMonitorService.HealthState.NA);
            }
        }
    },
    NUM_TCC_LOOP_NO_CHANGE(NUM_TCC_LOOP_NO_CHANGE_LABEL, false, MetricClass.TRANSACTIONS_METRIC, 5, 10, true, HealthMetricOutputType.INFLUX) {
        public void doSnapshot() {
            this.getHealthMetricData().addValue(TRUST_CHAIN_CONFIRMED_LABEL, HealthMetricOutputType.ALL, TRUST_CHAIN_CONFIRMED_LABEL, confirmationService.getTrustChainConfirmed());
            baseDoSnapshot(this, trustChainConfirmationService.getNumberOfTimesTrustScoreNotChanged());
        }

        @Override
        public void calculateHealthMetric() {
            calculateHealthValueMetricState(this);
        }
    },
    DSP_CONFIRMED_DELTA(DSP_CONFIRMED_LABEL_DELTA, true, MetricClass.TRANSACTIONS_METRIC, 2, 5, true, HealthMetricOutputType.INFLUX) {
        public void doSnapshot() {
            long dspConfirmed = confirmationService.getDspConfirmed();
            long totalTransactions = transactionHelper.getTotalTransactions();
            this.getHealthMetricData().addValue("DspConfirmed", HealthMetricOutputType.ALL, "DspConfirmed", dspConfirmed);
            baseDoSnapshot(this, totalTransactions - dspConfirmed);
        }

        @Override
        public void calculateHealthMetric() {
            baseCalculateHealthCounterMetricState(this);
        }
    },
    TOTAL_CONFIRMED(TOTAL_CONFIRMED_LABEL, true, MetricClass.TRANSACTIONS_METRIC, 0, 0, true, false, HealthMetricOutputType.ALL) {
        public void doSnapshot() {
            baseDoSnapshot(this, confirmationService.getTotalConfirmed());
        }

        @Override
        public void calculateHealthMetric() {
            HealthMetricData healthMetricData = monitorService.getHealthMetricData(this);
            healthMetricData.setLastHealthState(BaseNodeMonitorService.HealthState.NA);
        }
    },
    SOURCES_UPPER_BOUND(SOURCES_UPPER_BOUND_LABEL, false, MetricClass.TRANSACTIONS_METRIC, 24, 34, false, HealthMetricOutputType.INFLUX) {
        @Override
        public void doSnapshot() {
            long sources = clusterService.getTotalSources();
            this.getHealthMetricData().addValue("Sources", HealthMetricOutputType.ALL, "Sources", sources);
            baseDoSnapshot(this, sources);
        }

        @Override
        public void calculateHealthMetric() {
            calculateHealthValueMetricState(this);
        }
    },
    SOURCES_LOWER_BOUND(SOURCES_LOWER_BOUND_LABEL, false, MetricClass.TRANSACTIONS_METRIC, -8, -6, false, HealthMetricOutputType.INFLUX) {
        @Override
        public void doSnapshot() {
            baseDoSnapshot(this, clusterService.getTotalSources() * -1);
        }

        @Override
        public void calculateHealthMetric() {
            calculateHealthValueMetricState(this);
        }
    },
    INDEX_DELTA(INDEX_DELTA_LABEL, true, MetricClass.TRANSACTIONS_METRIC, 2, 4, true, HealthMetricOutputType.INFLUX) {
        public void doSnapshot() {
            long index = transactionIndexService.getLastTransactionIndexData().getIndex();
            this.getHealthMetricData().addValue("Index", HealthMetricOutputType.ALL, "Index", index);
            baseDoSnapshot(this, (transactionHelper.getTotalTransactions() - index) - 1);
        }

        @Override
        public void calculateHealthMetric() {
            baseCalculateHealthCounterMetricState(this);
        }
    },
    WAITING_DSP_CONSENSUS_RESULTS_CONFIRMED(WAITING_DSP_CONSENSUS_RESULTS_CONFIRMED_LABEL, true, MetricClass.TRANSACTIONS_METRIC, 1, 5, false, HealthMetricOutputType.ALL) {
        public void doSnapshot() {
            baseDoSnapshot(this, (long) confirmationService.getWaitingDspConsensusResultsMapSize());
        }

        @Override
        public void calculateHealthMetric() {
            baseCalculateHealthCounterMetricState(this);
        }
    },
    WAITING_MISSING_TRANSACTION_INDEXES(WAITING_MISSING_TRANSACTION_INDEXES_LABEL, true, MetricClass.TRANSACTIONS_METRIC, 1, 1, false, HealthMetricOutputType.ALL) {
        public void doSnapshot() {
            baseDoSnapshot(this, (long) confirmationService.getWaitingMissingTransactionIndexesSize());
        }

        @Override
        public void calculateHealthMetric() {
            baseCalculateHealthCounterMetricState(this);
        }
    },
    TOTAL_POSTPONED_TRANSACTIONS(TOTAL_POSTPONED_TRANSACTIONS_LABEL, true, MetricClass.TRANSACTIONS_METRIC, 2, 4, false, HealthMetricOutputType.ALL) {
        public void doSnapshot() {
            baseDoSnapshot(this, (long) transactionService.totalPostponedTransactions());
        }

        @Override
        public void calculateHealthMetric() {
            baseCalculateHealthCounterMetricState(this);
        }
    },
    WEB_SOCKET_MESSAGES_QUEUE(WEB_SOCKET_MESSAGES_QUEUE_LABEL, false, MetricClass.QUEUE_METRIC, 100, 1000, false, HealthMetricOutputType.ALL) {
        public void doSnapshot() {
            baseDoSnapshot(this, (long) webSocketMessageService.getMessageQueueSize());
        }

        @Override
        public void calculateHealthMetric() {
            baseCalculateHealthCounterMetricState(this);
        }
    },
    DCR_CONFIRMATION_QUEUE_SIZE(DCR_CONFIRMATION_QUEUE_SIZE_LABEL, false, MetricClass.QUEUE_METRIC, 100, 0, false, HealthMetricOutputType.INFLUX) {
        public void doSnapshot() {
            baseDoSnapshot(this, (long) confirmationService.getDcrConfirmationQueueSize());
        }

        @Override
        public void calculateHealthMetric() {
            baseCalculateHealthCounterMetricState(this);
        }
    },
    TCC_CONFIRMATION_QUEUE(TCC_CONFIRMATION_QUEUE_LABEL, false, MetricClass.QUEUE_METRIC, 100, 0, false, HealthMetricOutputType.ALL) {
        public void doSnapshot() {
            baseDoSnapshot(this, (long) confirmationService.getTccConfirmationQueueSize());
        }

        @Override
        public void calculateHealthMetric() {
            baseCalculateHealthCounterMetricState(this);
        }
    },
    PERCENTAGE_USED_HEAP_MEMORY(PERCENTAGE_USED_HEAP_MEMORY_LABEL, false, MetricClass.NA, 95, 98, false, HealthMetricOutputType.ALL) {
        public void doSnapshot() {
            baseDoSnapshot(this, (long) MemoryUtils.getPercentageUsedHeap());
        }

        @Override
        public void calculateHealthMetric() {
            calculateHealthValueMetricState(this);
        }
    },
    PERCENTAGE_USED_MEMORY(PERCENTAGE_USED_MEMORY_LABEL, false, MetricClass.NA, 85, 95, false, HealthMetricOutputType.ALL) {
        public void doSnapshot() {
            baseDoSnapshot(this, (long) MemoryUtils.getPercentageUsed());
        }

        @Override
        public void calculateHealthMetric() {
            calculateHealthValueMetricState(this);
        }
    },
    CONNECTED_TO_RECOVERY(CONNECTED_TO_RECOVERY_LABEL, true, MetricClass.TRANSACTIONS_METRIC, 1, 1, false, HealthMetricOutputType.INFLUX) {
        public void doSnapshot() {
            int notConnectedToRecovery = !networkService.isConnectedToRecovery() ? 1 : 0;
            baseDoSnapshot(this, (long) notConnectedToRecovery);
        }

        @Override
        public void calculateHealthMetric() {
            calculateHealthValueMetricState(this);
        }
    },
    TRANSACTION_PROPAGATION_QUEUE(TRANSACTION_PROPAGATION_QUEUE_LABEL, false, MetricClass.NA, 64, 0, false, HealthMetricOutputType.ALL) {
        public void doSnapshot() {
            baseDoSnapshot(this, (long) propagationSubscriber.getMessageQueueSize(ZeroMQSubscriberQueue.TRANSACTION));
        }

        @Override
        public void calculateHealthMetric() {
            baseCalculateHealthCounterMetricState(this);
        }
    },
    PROPAGATION_SUBSCRIBER_TRANSACTIONS_STATE_QUEUE_SIZE(PROPAGATION_SUBSCRIBER_TRANSACTIONS_STATE_QUEUE_SIZE_LABEL, false, MetricClass.QUEUE_METRIC, 5, 0, false, HealthMetricOutputType.ALL) {
        public void doSnapshot() {
            baseDoSnapshot(this, (long) propagationSubscriber.getMessageQueueSize(ZeroMQSubscriberQueue.TRANSACTIONS_STATE));
        }

        @Override
        public void calculateHealthMetric() {
            calculateHealthValueMetricState(this);
        }
    },
    PROPAGATION_SUBSCRIBER_NETWORK_QUEUE_SIZE(PROPAGATION_SUBSCRIBER_NETWORK_QUEUE_SIZE_LABEL, false, MetricClass.QUEUE_METRIC, 10, 0, false, HealthMetricOutputType.ALL) {
        public void doSnapshot() {
            baseDoSnapshot(this, (long) propagationSubscriber.getMessageQueueSize(ZeroMQSubscriberQueue.NETWORK));
        }

        @Override
        public void calculateHealthMetric() {
            calculateHealthValueMetricState(this);
        }
    },
    PROPAGATION_SUBSCRIBER_ADDRESS_QUEUE_SIZE(PROPAGATION_SUBSCRIBER_ADDRESS_QUEUE_SIZE_LABEL, false, MetricClass.QUEUE_METRIC, 40, 0, false, HealthMetricOutputType.ALL) {
        public void doSnapshot() {
            baseDoSnapshot(this, (long) propagationSubscriber.getMessageQueueSize(ZeroMQSubscriberQueue.ADDRESS));
        }

        @Override
        public void calculateHealthMetric() {
            calculateHealthValueMetricState(this);

        }
    },
    PROPAGATION_SUBSCRIBER_TRANSACTION_QUEUE_SIZE(PROPAGATION_SUBSCRIBER_TRANSACTION_QUEUE_SIZE_LABEL, false, MetricClass.QUEUE_METRIC, 40, 0, false, HealthMetricOutputType.ALL) {
        public void doSnapshot() {
            baseDoSnapshot(this, (long) propagationSubscriber.getMessageQueueSize(ZeroMQSubscriberQueue.TRANSACTION));
        }

        @Override
        public void calculateHealthMetric() {
            calculateHealthValueMetricState(this);
        }
    },
    PROPAGATION_SUBSCRIBER_HEARTBEAT_QUEUE_SIZE(PROPAGATION_SUBSCRIBER_HEARTBEAT_QUEUE_SIZE_LABEL, false, MetricClass.QUEUE_METRIC, 10, 0, false, HealthMetricOutputType.ALL) {
        public void doSnapshot() {
            baseDoSnapshot(this, (long) propagationSubscriber.getMessageQueueSize(ZeroMQSubscriberQueue.HEARTBEAT));
        }

        @Override
        public void calculateHealthMetric() {
            calculateHealthValueMetricState(this);
        }
    },
    ZERO_MQ_RECEIVER_QUEUE_SIZE(ZERO_MQ_RECEIVER_QUEUE_SIZE_LABEL, false, MetricClass.QUEUE_METRIC, 100, 0, false, HealthMetricOutputType.ALL) {
        public void doSnapshot() {
            baseDoSnapshot(this, (long) receiver.getQueueSize());
        }

        @Override
        public void calculateHealthMetric() {
            calculateHealthValueMetricState(this);
        }
    },
    PROPAGATION_PUBLISHER_QUEUE_SIZE(PROPAGATION_PUBLISHER_QUEUE_SIZE_LABEL, false, MetricClass.QUEUE_METRIC, 100, 0, false, HealthMetricOutputType.ALL) {
        public void doSnapshot() {
            baseDoSnapshot(this, (long) propagationPublisher.getQueueSize());
        }

        @Override
        public void calculateHealthMetric() {
            calculateHealthValueMetricState(this);
        }
    },
    LIVE_FILES_SIZE(LIVE_FILES_SIZE_LABEL, false, MetricClass.DATABASE_METRIC, 100, 0, false, HealthMetricOutputType.INFLUX) {
        public void doSnapshot() {
            if (dbRecoveryService != null && dbRecoveryService.isBackup()) {
                baseDoSnapshot(this, (long) databaseConnector.getLiveFilesNames().size());
            }
        }

        @Override
        public void calculateHealthMetric() {
            if (dbRecoveryService != null && dbRecoveryService.isBackup()) {
                calculateHealthValueMetricState(this);
            } else {
                monitorService.getHealthMetricData(this).setLastHealthState(BaseNodeMonitorService.HealthState.NA);
            }
        }
    },
    //    BACKUP_HOURLY(BACKUP_HOURLY_LABEL, false, MetricClass.BACKUP_METRIC, 2400, 4800, false) {
//        public void doSnapshot() {
//            long latestBackupStartedTime = dbRecoveryService.getBackupStartedTime();
//            HealthMetricData healthMetricData = monitorService.getHealthMetricData(this);
//            long prevBackupStartedTime = Math.max(0, healthMetricData.getSpecificLastMetricValue(LATEST_BACKUP_STARTED_TIME));
//            monitorService.getHealthMetricData(this).setSpecificLastMetricValue(PREVIOUS_BACKUP_STARTED_TIME, prevBackupStartedTime);
//            monitorService.getHealthMetricData(this).setSpecificLastMetricValue(LATEST_BACKUP_STARTED_TIME, latestBackupStartedTime);
//
//            if (latestBackupStartedTime > prevBackupStartedTime) {
//                monitorService.setMetricValue(this, dbRecoveryService.getBackupSuccess());
//            } else {
//                monitorService.setMetricValue(this, 0);
//            }
//            monitorService.getHealthMetricData(this).setSpecificLastMetricValue(SNAPSHOT_IN_SECONDS, java.time.Instant.now().getEpochSecond());
//            monitorService.setSnapshotTime(this, Instant.now());
//        }
//
//        @Override
//        public void calculateHealthMetric() {
//            HealthMetricData healthMetricData = monitorService.getHealthMetricData(this);
//            if (dbRecoveryService != null && dbRecoveryService.isBackup()) {
//                healthMetricData.setLastConditionValue(healthMetricData.getSpecificLastMetricValue(SNAPSHOT_IN_SECONDS) - healthMetricData.getSpecificLastMetricValue(LATEST_BACKUP_STARTED_TIME));
//                HealthMetric.calculateHealthValueMetricState(healthMetricData, this);
//            } else {
//                healthMetricData.setLastHealthState(BaseNodeMonitorService.HealthState.NA);
//            }
//        }
//    },
    LAST_BACKUP_ELAPSED(LAST_BACKUP_ELAPSED_LABEL, false, MetricClass.BACKUP_METRIC, 3600, 0, false, HealthMetricOutputType.INFLUX) {
        public void doSnapshot() {
            if (dbRecoveryService != null && dbRecoveryService.isBackup()) {
                long backupStartedTime = dbRecoveryService.getBackupStartedTime();
                this.getHealthMetricData().addValue("backupTime", HealthMetricOutputType.INFLUX, "backupStartedTime", backupStartedTime);
                baseDoSnapshot(this, java.time.Instant.now().getEpochSecond() - backupStartedTime);
            }
        }

        @Override
        public void calculateHealthMetric() {
            if (dbRecoveryService != null && dbRecoveryService.isBackup()) {
                HealthMetric.calculateHealthValueMetricState(this);
            } else {
                monitorService.getHealthMetricData(this).setLastHealthState(BaseNodeMonitorService.HealthState.NA);
            }
        }
    },
    NUMBER_OF_LIVE_FILES_NOT_BACKED_UP(NUMBER_OF_LIVE_FILES_NOT_BACKED_UP_LABEL, false, MetricClass.BACKUP_METRIC, 1, 0, false, HealthMetricOutputType.INFLUX) {
        public void doSnapshot() {
            if (dbRecoveryService != null && dbRecoveryService.isBackup()) {
                long numberOfBackedFiles = dbRecoveryService.getLastBackupInfo().numberFiles();
                long numberOfLiveFiles = databaseConnector.getLiveFilesNames().size();
                this.getHealthMetricData().addValue("BackupNumberOfFiles", HealthMetricOutputType.INFLUX, "BackupNumberOfFiles", numberOfBackedFiles);
                baseDoSnapshot(this, numberOfLiveFiles - numberOfBackedFiles);
            }
        }

        @Override
        public void calculateHealthMetric() {
            if (dbRecoveryService != null && dbRecoveryService.isBackup()) {
                HealthMetric.calculateHealthValueMetricState(this);
            } else {
                monitorService.getHealthMetricData(this).setLastHealthState(BaseNodeMonitorService.HealthState.NA);
            }
        }
    },
    BACKUP_SIZE(BACKUP_SIZE_LABEL, false, MetricClass.BACKUP_METRIC, 0, 0, false, HealthMetricOutputType.INFLUX) {
        public void doSnapshot() {
            if (dbRecoveryService != null && dbRecoveryService.isBackup()) {
                baseDoSnapshot(this, dbRecoveryService.getLastBackupInfo().size());
            }
        }

        @Override
        public void calculateHealthMetric() {
            monitorService.getHealthMetricData(this).setLastHealthState(BaseNodeMonitorService.HealthState.NA);
        }
    },
    BACKUP_ENTIRE_DURATION(BACKUP_ENTIRE_DURATION_LABEL, false, MetricClass.BACKUP_METRIC, 75, 180, false, HealthMetricOutputType.INFLUX) {
        public void doSnapshot() {
            if (dbRecoveryService != null && dbRecoveryService.isBackup()) {
                baseDoSnapshot(this, dbRecoveryService.getEntireDuration());
            }
        }

        @Override
        public void calculateHealthMetric() {
            if (dbRecoveryService != null && dbRecoveryService.isBackup()) {
                HealthMetric.calculateHealthValueMetricState(this);
            } else {
                monitorService.getHealthMetricData(this).setLastHealthState(BaseNodeMonitorService.HealthState.NA);
            }
        }
    },
    BACKUP_DURATION(BACKUP_DURATION_LABEL, false, MetricClass.BACKUP_METRIC, 45, 90, false, HealthMetricOutputType.INFLUX) {
        public void doSnapshot() {
            if (dbRecoveryService != null && dbRecoveryService.isBackup()) {
                baseDoSnapshot(this, dbRecoveryService.getBackupDuration());
            }
        }

        @Override
        public void calculateHealthMetric() {
            if (dbRecoveryService != null && dbRecoveryService.isBackup()) {
                HealthMetric.calculateHealthValueMetricState(this);
            } else {
                monitorService.getHealthMetricData(this).setLastHealthState(BaseNodeMonitorService.HealthState.NA);
            }
        }
    },
    BACKUP_UPLOAD_DURATION(BACKUP_UPLOAD_DURATION_LABEL, false, MetricClass.BACKUP_METRIC, 20, 60, false, HealthMetricOutputType.INFLUX) {
        public void doSnapshot() {
            if (dbRecoveryService != null && dbRecoveryService.isBackup()) {
                baseDoSnapshot(this, dbRecoveryService.getUploadDuration());
            }
        }

        @Override
        public void calculateHealthMetric() {
            if (dbRecoveryService != null && dbRecoveryService.isBackup()) {
                HealthMetric.calculateHealthValueMetricState(this);
            } else {
                monitorService.getHealthMetricData(this).setLastHealthState(BaseNodeMonitorService.HealthState.NA);
            }
        }
    },
    BACKUP_REMOVAL_DURATION(BACKUP_REMOVAL_DURATION_LABEL, false, MetricClass.BACKUP_METRIC, 10, 30, false, HealthMetricOutputType.INFLUX) {
        public void doSnapshot() {
            if (dbRecoveryService != null && dbRecoveryService.isBackup()) {
                baseDoSnapshot(this, dbRecoveryService.getRemovalDuration());
            }
        }

        @Override
        public void calculateHealthMetric() {
            if (dbRecoveryService != null && dbRecoveryService.isBackup()) {
                HealthMetric.calculateHealthValueMetricState(this);
            } else {
                monitorService.getHealthMetricData(this).setLastHealthState(BaseNodeMonitorService.HealthState.NA);
            }
        }
    };

    protected static IMonitorService monitorService;
    protected static ITransactionHelper transactionHelper;
    protected static IClusterService clusterService;
    protected static TransactionIndexService transactionIndexService;
    protected static IConfirmationService confirmationService;
    protected static TrustChainConfirmationService trustChainConfirmationService;
    protected static ITransactionService transactionService;
    protected static IPropagationSubscriber propagationSubscriber;
    protected static IWebSocketMessageService webSocketMessageService;
    protected static INetworkService networkService;
    protected static IReceiver receiver;
    protected static IPropagationPublisher propagationPublisher;
    protected static IDatabaseConnector databaseConnector;
    protected static IDBRecoveryService dbRecoveryService;
    private String label;
    private boolean counterBased;
    private boolean detailedLogs;
    private HealthMetricOutputType healthMetricOutputType;
    private MetricClass metricClass;
    private long warningThreshold;
    private long criticalThreshold;
    private boolean includeInTotalHealthState = true;
    private static final Map<String, HealthMetric> BY_LABEL = new HashMap<>();

    static {
        for (HealthMetric hm : values()) {
            BY_LABEL.put(hm.label, hm);
        }
    }

    HealthMetric(String label, boolean counterBased, MetricClass metricClass, long warningThreshold, long criticalThreshold, boolean detailedLogs, HealthMetricOutputType healthMetricOutputType) {
        setHealthMetricBaseProperties(label, counterBased, metricClass, warningThreshold, criticalThreshold, detailedLogs, healthMetricOutputType);
    }

    HealthMetric(String label, boolean counterBased, MetricClass metricClass, long warningThreshold, long criticalThreshold, boolean detailedLogs, boolean includeInTotalHealthState, HealthMetricOutputType healthMetricOutputType) {
        setHealthMetricBaseProperties(label, counterBased, metricClass, warningThreshold, criticalThreshold, detailedLogs, healthMetricOutputType);
        this.includeInTotalHealthState = includeInTotalHealthState;
    }

    public static HealthMetric getHealthMetricByLabel(String label) {
        HealthMetric value = BY_LABEL.get(label);
        if (value != null) {
            return value;
        } else {
            throw new IllegalArgumentException("No metric found");
        }
    }

    private static void baseCalculateHealthCounterMetricState(HealthMetric healthMetric) {
        HealthMetricData healthMetricData = monitorService.getHealthMetricData(healthMetric);
        calculateHealthCounterMetricState(healthMetricData, healthMetric, true);
    }

    private static synchronized void baseDoSnapshot(HealthMetric healthMetric, Long metricValue) {
        monitorService.setMetricValue(healthMetric, metricValue);
        monitorService.setSnapshotTime(healthMetric, Instant.now());
    }

    public String getLabel() {
        return this.label;
    }

    private static boolean healthIsDegrading(HealthMetricData healthMetricData) {
        return Math.abs(healthMetricData.getMetricValue()) > Math.abs(healthMetricData.getPreviousMetricValue());
    }

    private static void calculateHealthCounterMetricState(HealthMetricData healthMetricData, HealthMetric healthMetric, boolean updateCounter) {
        if (healthMetricData.getMetricValue() != 0) {
            if (updateCounter && healthIsDegrading(healthMetricData)) {
                healthMetricData.increaseDegradingCounter();
            }
            if (healthMetricData.getDegradingCounter() >= healthMetric.criticalThreshold && healthMetric.criticalThreshold >= healthMetric.warningThreshold) {
                healthMetricData.setLastHealthState(BaseNodeMonitorService.HealthState.CRITICAL);
            } else if (healthMetricData.getDegradingCounter() >= healthMetric.warningThreshold) {
                healthMetricData.setLastHealthState(BaseNodeMonitorService.HealthState.WARNING);
            } else if (BaseNodeMonitorService.HealthState.NA.equals(healthMetricData.getLastHealthState())) {
                healthMetricData.setLastHealthState(BaseNodeMonitorService.HealthState.NORMAL);
            }
        } else {
            if (updateCounter) {
                healthMetricData.setDegradingCounter(0);
            }
            healthMetricData.setLastHealthState(BaseNodeMonitorService.HealthState.NORMAL);
        }
    }

    private static void calculateHealthValueMetricState(HealthMetric healthMetric) {
        HealthMetricData healthMetricData = monitorService.getHealthMetricData(healthMetric);
        long currentMetricValue = healthMetricData.getMetricValue();
        if (currentMetricValue >= healthMetric.criticalThreshold && healthMetric.criticalThreshold >= healthMetric.warningThreshold) {
            healthMetricData.setLastHealthState(BaseNodeMonitorService.HealthState.CRITICAL);
        } else if (currentMetricValue >= healthMetric.warningThreshold) {
            healthMetricData.setLastHealthState(BaseNodeMonitorService.HealthState.WARNING);
        } else {
            healthMetricData.setLastHealthState(BaseNodeMonitorService.HealthState.NORMAL);
        }
    }

    private void setHealthMetricBaseProperties(String label, boolean counterBased, MetricClass metricClass, long warningThreshold, long criticalThreshold, boolean detailedLogs, HealthMetricOutputType healthMetricOutputType) {
        setHealthMetricBaseProperties(label, counterBased, metricClass, warningThreshold, criticalThreshold, detailedLogs);
        this.healthMetricOutputType = healthMetricOutputType;
    }

    private void setHealthMetricBaseProperties(String label, boolean counterBased, MetricClass metricClass, long warningThreshold, long criticalThreshold, boolean detailedLogs) {
        this.label = label;
        this.counterBased = counterBased;
        this.metricClass = metricClass;
        this.warningThreshold = warningThreshold;
        this.criticalThreshold = criticalThreshold;
        this.detailedLogs = detailedLogs;
    }

    public MetricClass getMetricType() {
        return metricClass;
    }

    public long getWarningThreshold() {
        return warningThreshold;
    }

    public long getCriticalThreshold() {
        return criticalThreshold;
    }

    public void setThresholds(long warningThreshold, long criticalThreshold) {
        this.warningThreshold = warningThreshold;
        this.criticalThreshold = criticalThreshold;
    }

    public boolean isCounterBased() {
        return counterBased;
    }

    public boolean isDetailedLogs() {
        return detailedLogs;
    }

    @Override
    public HealthMetricData getHealthMetricData() {
        return monitorService.getHealthMetricData(this);
    }

    @Override
    public Health getHealthBuilder(Health.Builder builder) {
        HealthMetricData healthMetricData = getHealthMetricData();
        if (healthMetricData.getLastHealthState().ordinal() == BaseNodeMonitorService.HealthState.CRITICAL.ordinal()) {
            builder.down();
        } else {
            builder.up();
        }
        builder.withDetail(this.label, healthMetricData.getLastHealthState()).withDetail("State", healthMetricData.getLastHealthState()).withDetail("conditionValue", healthMetricData.getMetricValue());
        if (this.isCounterBased()) {
            builder.withDetail("Counter", healthMetricData.getDegradingCounter());
        }
        healthMetricData.getAdditionalValues().forEach((key, value) -> builder.withDetail(key, value.toString()));
        return builder.build();
    }

    public HealthMetricOutputType getHealthMetricOutputType() {
        return healthMetricOutputType;
    }

    public MetricClass getMetricClass() {
        return metricClass;
    }

}
