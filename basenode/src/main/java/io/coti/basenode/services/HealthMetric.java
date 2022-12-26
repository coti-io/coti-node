package io.coti.basenode.services;

import io.coti.basenode.communication.ZeroMQSubscriberQueue;
import io.coti.basenode.communication.interfaces.IPropagationPublisher;
import io.coti.basenode.communication.interfaces.IPropagationSubscriber;
import io.coti.basenode.communication.interfaces.IReceiver;
import io.coti.basenode.data.HealthMetricData;
import io.coti.basenode.data.HealthMetricOutputType;
import io.coti.basenode.data.MetricClass;
import io.coti.basenode.database.interfaces.IDatabaseConnector;
import io.coti.basenode.model.RejectedTransactions;
import io.coti.basenode.services.interfaces.*;
import io.coti.basenode.utilities.MemoryUtils;
import lombok.Getter;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static io.coti.basenode.constants.BaseNodeHealthMetricConstants.*;


public enum HealthMetric implements IHealthMetric {

    TOTAL_TRANSACTIONS_DELTA(TOTAL_TRANSACTIONS_DELTA_LABEL, MetricClass.TRANSACTIONS_METRIC, 0, 0, true, HealthMetricOutputType.INFLUX) {
        @Override
        public void doSnapshot() {
            long totalTransactions = transactionHelper.getTotalTransactions();
            HealthMetricData healthMetricData = this.getHealthMetricData();
            healthMetricData.addValue(TOTAL_TRANSACTIONS_LABEL, HealthMetricOutputType.ALL, TOTAL_TRANSACTIONS_LABEL, totalTransactions);
            long totalTransactionsFromRecoveryServer = transactionHelper.getTotalNumberOfTransactionsFromRecovery();
            if (totalTransactionsFromRecoveryServer > 0) {
                healthMetricData.addValue(TOTAL_TRANSACTIONS_FROM_RECOVERY_LABEL, HealthMetricOutputType.INFLUX, TOTAL_TRANSACTIONS_FROM_RECOVERY_LABEL, totalTransactionsFromRecoveryServer);
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

        @Override
        public String getDescription() {
            return "The difference between amount of transactions declared by recovery node and total amount of transactions registered locally";
        }
    },
    NUM_TCC_LOOP_NO_CHANGE(NUM_TCC_LOOP_NO_CHANGE_LABEL, MetricClass.TRANSACTIONS_METRIC, 5, 10, true, HealthMetricOutputType.INFLUX) {
        public void doSnapshot() {
            this.getHealthMetricData().addValue(TRUST_CHAIN_CONFIRMED_LABEL, HealthMetricOutputType.ALL, TRUST_CHAIN_CONFIRMED_LABEL, confirmationService.getTrustChainConfirmed());
            baseDoSnapshot(this, trustChainConfirmationService.getNumberOfTimesTrustScoreNotChanged());
        }

        @Override
        public void calculateHealthMetric() {
            calculateHealthValueMetricState(this);
        }

        @Override
        public String getDescription() {
            return "Total amount of transactions with Trust Chain Confirmed";
        }
    },
    DSP_CONFIRMED_DELTA(DSP_CONFIRMED_LABEL_DELTA, MetricClass.TRANSACTIONS_METRIC, 2, 5, true, HealthMetricOutputType.INFLUX) {
        public void doSnapshot() {
            long dspConfirmed = confirmationService.getDspConfirmed();
            long totalTransactions = transactionHelper.getTotalTransactions();
            this.getHealthMetricData().addValue(DSP_CONFIRMED_LABEL, HealthMetricOutputType.ALL, DSP_CONFIRMED_LABEL, dspConfirmed);
            baseDoSnapshot(this, totalTransactions - dspConfirmed);
        }

        @Override
        public void calculateHealthMetric() {
            baseCalculateHealthCounterMetricState(this);
        }

        @Override
        public String getDescription() {
            return "Total amount of transactions with DSP Confirmed";
        }
    },
    TOTAL_CONFIRMED(TOTAL_CONFIRMED_LABEL, MetricClass.TRANSACTIONS_METRIC, 0, 0, true, HealthMetricOutputType.ALL) {
        public void doSnapshot() {
            baseDoSnapshot(this, confirmationService.getTotalConfirmed());
        }

        @Override
        public void calculateHealthMetric() {
            HealthMetricData healthMetricData = monitorService.getHealthMetricData(this);
            healthMetricData.setLastHealthState(BaseNodeMonitorService.HealthState.NA);
        }

        @Override
        public String getDescription() {
            return "Total amount of transactions with both DSP Confirmed & Trust Chain Confirmed";
        }
    },
    INDEX_DELTA(INDEX_DELTA_LABEL, MetricClass.TRANSACTIONS_METRIC, 2, 4, true, HealthMetricOutputType.INFLUX) {
        public void doSnapshot() {
            long index = transactionIndexService.getLastTransactionIndexData().getIndex();
            this.getHealthMetricData().addValue(INDEX_LABEL, HealthMetricOutputType.ALL, INDEX_LABEL, index);
            baseDoSnapshot(this, (transactionHelper.getTotalTransactions() - index) - 1);
        }

        @Override
        public void calculateHealthMetric() {
            baseCalculateHealthCounterMetricState(this);
        }

        @Override
        public String getDescription() {
            return "Highest contiguous index value, should be one less than the amount of total transactions";
        }
    },
    SOURCES_UPPER_BOUND(SOURCES_UPPER_BOUND_LABEL, MetricClass.TRANSACTIONS_METRIC, 24, 34, false, HealthMetricOutputType.INFLUX) {
        @Override
        public void doSnapshot() {
            long sources = clusterService.getTotalSources();
            this.getHealthMetricData().addValue(SOURCES_LABEL, HealthMetricOutputType.ALL, SOURCES_LABEL, sources);
            baseDoSnapshot(this, sources);
        }

        @Override
        public void calculateHealthMetric() {
            calculateHealthValueMetricState(this);
        }

        @Override
        public String getDescription() {
            return "Upper aloud bound of total amount of transactions {including Zero spend transactions} that are sources";
        }
    },
    SOURCES_LOWER_BOUND(SOURCES_LOWER_BOUND_LABEL, MetricClass.TRANSACTIONS_METRIC, -8, -6, false, HealthMetricOutputType.INFLUX) {
        @Override
        public void doSnapshot() {
            baseDoSnapshot(this, clusterService.getTotalSources() * -1);
        }

        @Override
        public void calculateHealthMetric() {
            calculateHealthValueMetricState(this);
        }

        @Override
        public String getDescription() {
            return "Lower aloud bound of total amount of transactions {including Zero spend transactions} that are sources";
        }
    },
    DCR_CONFIRMATION_QUEUE_SIZE(DCR_CONFIRMATION_QUEUE_SIZE_LABEL, MetricClass.QUEUE_METRIC, 100, 0, false, HealthMetricOutputType.INFLUX) {
        public void doSnapshot() {
            baseDoSnapshot(this, (long) confirmationService.getDcrConfirmationQueueSize());
        }

        @Override
        public void calculateHealthMetric() {
            baseCalculateHealthCounterMetricState(this);
        }

        @Override
        public String getDescription() {
            return "Amount of DCRs that are waiting for processing and confirmation.";
        }
    },
    WAITING_DCR_QUEUE(WAITING_DCR_QUEUE_LABEL, MetricClass.TRANSACTIONS_METRIC, 1, 5, false, HealthMetricOutputType.ALL) {
        public void doSnapshot() {
            baseDoSnapshot(this, (long) confirmationService.getWaitingDspConsensusResultsMapSize());
        }

        @Override
        public void calculateHealthMetric() {
            baseCalculateHealthCounterMetricState(this);
        }

        @Override
        public String getDescription() {
            return "Amount of transactions with DSP Consensus that await to be indexed according to expected sequential order.";
        }
    },
    TCC_CONFIRMATION_QUEUE(TCC_CONFIRMATION_QUEUE_LABEL, MetricClass.QUEUE_METRIC, 100, 0, false, HealthMetricOutputType.INFLUX) {
        public void doSnapshot() {
            baseDoSnapshot(this, (long) confirmationService.getTccConfirmationQueueSize());
        }

        @Override
        public void calculateHealthMetric() {
            baseCalculateHealthCounterMetricState(this);
        }

        @Override
        public String getDescription() {
            return "Amount of TCCs that are waiting for processing and confirmation.";
        }
    },
    WAITING_MISSING_TRANSACTION_INDEXES(WAITING_MISSING_TRANSACTION_INDEXES_LABEL, MetricClass.TRANSACTIONS_METRIC, 1, 1, false, HealthMetricOutputType.ALL) {
        public void doSnapshot() {
            baseDoSnapshot(this, (long) confirmationService.getWaitingMissingTransactionIndexesSize());
        }

        @Override
        public void calculateHealthMetric() {
            baseCalculateHealthCounterMetricState(this);
        }

        @Override
        public String getDescription() {
            return "Amount of missing transactions that have yet to be indexed, either because of the expected sequential order or because of lack of DSP Consensus.";
        }
    },
    TOTAL_POSTPONED_TRANSACTIONS(TOTAL_POSTPONED_TRANSACTIONS_LABEL, MetricClass.TRANSACTIONS_METRIC, 2, 4, false, HealthMetricOutputType.ALL) {
        public void doSnapshot() {
            baseDoSnapshot(this, (long) transactionService.totalPostponedTransactions());
        }

        @Override
        public void calculateHealthMetric() {
            baseCalculateHealthCounterMetricState(this);
        }

        @Override
        public String getDescription() {
            return "Total amount of transactions that are considered postponed due to missing parents.";
        }
    },
    WEB_SOCKET_MESSAGES_QUEUE(WEB_SOCKET_MESSAGES_QUEUE_LABEL, MetricClass.QUEUE_METRIC, 100, 1000, false, HealthMetricOutputType.ALL) {
        public void doSnapshot() {
            baseDoSnapshot(this, (long) webSocketMessageService.getMessageQueueSize());
        }

        @Override
        public void calculateHealthMetric() {
            baseCalculateHealthCounterMetricState(this);
        }

        @Override
        public String getDescription() {
            return "Queue size for websocket messages.";
        }
    },
    PERCENTAGE_USED_HEAP_MEMORY(PERCENTAGE_USED_HEAP_MEMORY_LABEL, MetricClass.SYSTEM_METRIC, 95, 98, false, HealthMetricOutputType.ALL) {
        public void doSnapshot() {
            baseDoSnapshot(this, (long) MemoryUtils.getPercentageUsedHeap());
        }

        @Override
        public void calculateHealthMetric() {
            calculateHealthValueMetricState(this);
        }

        @Override
        public String getDescription() {
            return "Used heap memory in percents";
        }
    },
    PERCENTAGE_USED_MEMORY(PERCENTAGE_USED_MEMORY_LABEL, MetricClass.SYSTEM_METRIC, 85, 95, false, HealthMetricOutputType.ALL) {
        public void doSnapshot() {
            baseDoSnapshot(this, (long) MemoryUtils.getPercentageUsed());
        }

        @Override
        public void calculateHealthMetric() {
            calculateHealthValueMetricState(this);
        }

        @Override
        public String getDescription() {
            return "Used memory in percents";
        }
    },
    CONNECTED_TO_RECOVERY(CONNECTED_TO_RECOVERY_LABEL, MetricClass.TRANSACTIONS_METRIC, 1, 1, false, HealthMetricOutputType.INFLUX) {
        public void doSnapshot() {
            int notConnectedToRecovery = !networkService.isConnectedToRecovery() ? 1 : 0;
            baseDoSnapshot(this, (long) notConnectedToRecovery);
        }

        @Override
        public void calculateHealthMetric() {
            calculateHealthValueMetricState(this);
        }

        @Override
        public String getDescription() {
            return "Indicates weather the node is connected to recovery server.";
        }
    },
    TRANSACTION_PROPAGATION_QUEUE(TRANSACTION_PROPAGATION_QUEUE_LABEL, MetricClass.QUEUE_METRIC, 64, 0, false, HealthMetricOutputType.INFLUX) {
        public void doSnapshot() {
            baseDoSnapshot(this, (long) propagationSubscriber.getMessageQueueSize(ZeroMQSubscriberQueue.TRANSACTION));
        }

        @Override
        public void calculateHealthMetric() {
            baseCalculateHealthCounterMetricState(this);
        }

        @Override
        public String getDescription() {
            return "Propagation queue size for Transactions";
        }
    },
    PROPAGATION_SUBSCRIBER_TRANSACTIONS_STATE_QUEUE_SIZE(PROPAGATION_SUBSCRIBER_TRANSACTIONS_STATE_QUEUE_SIZE_LABEL, MetricClass.QUEUE_METRIC, 5, 0, false, HealthMetricOutputType.INFLUX) {
        public void doSnapshot() {
            baseDoSnapshot(this, (long) propagationSubscriber.getMessageQueueSize(ZeroMQSubscriberQueue.TRANSACTIONS_STATE));
        }

        @Override
        public void calculateHealthMetric() {
            calculateHealthValueMetricState(this);
        }

        @Override
        public String getDescription() {
            return "The size of the propagation subscriber queue of Transactions State";
        }
    },
    PROPAGATION_SUBSCRIBER_NETWORK_QUEUE_SIZE(PROPAGATION_SUBSCRIBER_NETWORK_QUEUE_SIZE_LABEL, MetricClass.QUEUE_METRIC, 10, 0, false, HealthMetricOutputType.INFLUX) {
        public void doSnapshot() {
            baseDoSnapshot(this, (long) propagationSubscriber.getMessageQueueSize(ZeroMQSubscriberQueue.NETWORK));
        }

        @Override
        public void calculateHealthMetric() {
            calculateHealthValueMetricState(this);
        }

        @Override
        public String getDescription() {
            return "The size of the propagation subscriber queue of Network State";
        }
    },
    PROPAGATION_SUBSCRIBER_ADDRESS_QUEUE_SIZE(PROPAGATION_SUBSCRIBER_ADDRESS_QUEUE_SIZE_LABEL, MetricClass.QUEUE_METRIC, 40, 0, false, HealthMetricOutputType.INFLUX) {
        public void doSnapshot() {
            baseDoSnapshot(this, (long) propagationSubscriber.getMessageQueueSize(ZeroMQSubscriberQueue.ADDRESS));
        }

        @Override
        public void calculateHealthMetric() {
            calculateHealthValueMetricState(this);

        }

        @Override
        public String getDescription() {
            return "The size of the propagation subscriber queue of Address";
        }
    },
    PROPAGATION_SUBSCRIBER_TRANSACTION_QUEUE_SIZE(PROPAGATION_SUBSCRIBER_TRANSACTION_QUEUE_SIZE_LABEL, MetricClass.QUEUE_METRIC, 40, 0, false, HealthMetricOutputType.INFLUX) {
        public void doSnapshot() {
            baseDoSnapshot(this, (long) propagationSubscriber.getMessageQueueSize(ZeroMQSubscriberQueue.TRANSACTION));
        }

        @Override
        public void calculateHealthMetric() {
            calculateHealthValueMetricState(this);
        }

        @Override
        public String getDescription() {
            return "The size of the propagation subscriber queue of Transaction";
        }
    },
    PROPAGATION_SUBSCRIBER_HEARTBEAT_QUEUE_SIZE(PROPAGATION_SUBSCRIBER_HEARTBEAT_QUEUE_SIZE_LABEL, MetricClass.QUEUE_METRIC, 10, 0, false, HealthMetricOutputType.INFLUX) {
        public void doSnapshot() {
            baseDoSnapshot(this, (long) propagationSubscriber.getMessageQueueSize(ZeroMQSubscriberQueue.HEARTBEAT));
        }

        @Override
        public void calculateHealthMetric() {
            calculateHealthValueMetricState(this);
        }

        @Override
        public String getDescription() {
            return "The size of the propagation subscriber queue of Heartbeat";
        }
    },
    ZERO_MQ_RECEIVER_QUEUE_SIZE(ZERO_MQ_RECEIVER_QUEUE_SIZE_LABEL, MetricClass.QUEUE_METRIC, 100, 0, false, HealthMetricOutputType.INFLUX) {
        public void doSnapshot() {
            baseDoSnapshot(this, (long) receiver.getQueueSize());
        }

        @Override
        public void calculateHealthMetric() {
            calculateHealthValueMetricState(this);
        }

        @Override
        public String getDescription() {
            return "The size of the ZeroMQ Receiver queue";
        }
    },
    PROPAGATION_PUBLISHER_QUEUE_SIZE(PROPAGATION_PUBLISHER_QUEUE_SIZE_LABEL, MetricClass.QUEUE_METRIC, 100, 0, false, HealthMetricOutputType.INFLUX) {
        public void doSnapshot() {
            baseDoSnapshot(this, (long) propagationPublisher.getQueueSize());
        }

        @Override
        public void calculateHealthMetric() {
            calculateHealthValueMetricState(this);
        }

        @Override
        public String getDescription() {
            return "The size of the propagation publisher queue";
        }
    },
    LIVE_FILES_SIZE(LIVE_FILES_SIZE_LABEL, MetricClass.DATABASE_METRIC, 100, 0, false, HealthMetricOutputType.INFLUX) {
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

        @Override
        public String getDescription() {
            return "The size of the live files";
        }
    },
    LAST_BACKUP_ELAPSED(LAST_BACKUP_ELAPSED_LABEL, MetricClass.BACKUP_METRIC, 3600, 0, false, HealthMetricOutputType.INFLUX) {
        public void doSnapshot() {
            if (dbRecoveryService != null && dbRecoveryService.isBackup()) {
                long backupStartedTime = dbRecoveryService.getBackupStartedTime();
                this.getHealthMetricData().addValue(BACKUP_STARTED_TIME_LABEL, HealthMetricOutputType.INFLUX, BACKUP_STARTED_TIME_LABEL, backupStartedTime);
                baseDoSnapshot(this, java.time.Instant.now().getEpochSecond() - backupStartedTime);

                if (dbRecoveryService.getBackUpLog().size() > 0) {
                    dbRecoveryService.clearBackupLog();
                }
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

        @Override
        public String getDescription() {
            return "";
        }
    },
    NUMBER_OF_LIVE_FILES_NOT_BACKED_UP(NUMBER_OF_LIVE_FILES_NOT_BACKED_UP_LABEL, MetricClass.BACKUP_METRIC, 1, 0, false, HealthMetricOutputType.INFLUX) {
        public void doSnapshot() {
            if (dbRecoveryService != null && dbRecoveryService.isBackup()) {
                long numberOfBackedFiles = dbRecoveryService.getLastBackupInfo().numberFiles();
                long numberOfLiveFiles = databaseConnector.getLiveFilesNames().size();
                this.getHealthMetricData().addValue(BACKED_UP_NUMBER_OF_FILES_LABEL, HealthMetricOutputType.INFLUX, BACKED_UP_NUMBER_OF_FILES_LABEL, numberOfBackedFiles);
                baseDoSnapshot(this, numberOfLiveFiles - numberOfBackedFiles);
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

        @Override
        public String getDescription() {
            return "";
        }
    },
    BACKUP_SIZE(BACKUP_SIZE_LABEL, MetricClass.BACKUP_METRIC, 0, 0, false, HealthMetricOutputType.INFLUX) {
        public void doSnapshot() {
            if (dbRecoveryService != null && dbRecoveryService.isBackup()) {
                baseDoSnapshot(this, dbRecoveryService.getLastBackupInfo().size());
            }
        }

        @Override
        public void calculateHealthMetric() {
            monitorService.getHealthMetricData(this).setLastHealthState(BaseNodeMonitorService.HealthState.NA);
        }

        @Override
        public String getDescription() {
            return "Amount of succesful backups in period";
        }
    },
    BACKUP_ENTIRE_DURATION(BACKUP_ENTIRE_DURATION_LABEL, MetricClass.BACKUP_METRIC, 75, 180, false, HealthMetricOutputType.INFLUX) {
        public void doSnapshot() {
            if (dbRecoveryService != null && dbRecoveryService.isBackup()) {
                baseDoSnapshot(this, dbRecoveryService.getEntireDuration());
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

        @Override
        public String getDescription() {
            return "Duration for entire backup process";
        }
    },
    BACKUP_DURATION(BACKUP_DURATION_LABEL, MetricClass.BACKUP_METRIC, 45, 90, false, HealthMetricOutputType.INFLUX) {
        public void doSnapshot() {
            if (dbRecoveryService != null && dbRecoveryService.isBackup()) {
                baseDoSnapshot(this, dbRecoveryService.getBackupDuration());
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

        @Override
        public String getDescription() {
            return "Duration for backup";
        }
    },
    BACKUP_UPLOAD_DURATION(BACKUP_UPLOAD_DURATION_LABEL, MetricClass.BACKUP_METRIC, 20, 60, false, HealthMetricOutputType.INFLUX) {
        public void doSnapshot() {
            if (dbRecoveryService != null && dbRecoveryService.isBackup()) {
                baseDoSnapshot(this, dbRecoveryService.getUploadDuration());
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

        @Override
        public String getDescription() {
            return "Duration for backup upload";
        }
    },
    BACKUP_REMOVAL_DURATION(BACKUP_REMOVAL_DURATION_LABEL, MetricClass.BACKUP_METRIC, 10, 30, false, HealthMetricOutputType.INFLUX) {
        public void doSnapshot() {
            if (dbRecoveryService != null && dbRecoveryService.isBackup()) {
                baseDoSnapshot(this, dbRecoveryService.getRemovalDuration());
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

        @Override
        public String getDescription() {
            return "Duration for backup removal";
        }
    },
    REJECTED_TRANSACTIONS(REJECTED_TRANSACTIONS_LABEL, MetricClass.TRANSACTIONS_METRIC, 10, 0, true, HealthMetricOutputType.INFLUX) {
        @Override
        public void doSnapshot() {
            baseDoSnapshot(this, rejectedTransactions.size());
        }

        @Override
        public void calculateHealthMetric() {
            calculateHealthValueMetricState(this);
        }

        @Override
        public String getDescription() {
            return "Amount of rejected transactions during the current day";
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
    protected static RejectedTransactions rejectedTransactions;
    @Getter
    private String label;
    @Getter
    private boolean detailedLogs;
    @Getter
    private HealthMetricOutputType healthMetricOutputType;
    @Getter
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

    HealthMetric(String label, MetricClass metricClass, long warningThreshold, long criticalThreshold, boolean detailedLogs, HealthMetricOutputType healthMetricOutputType) {
        setHealthMetricBasePropertiesAndOutput(label, metricClass, warningThreshold, criticalThreshold, detailedLogs, healthMetricOutputType);
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
        calculateHealthCounterMetricState(healthMetricData, healthMetric);
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

    private static void calculateHealthCounterMetricState(HealthMetricData healthMetricData, HealthMetric healthMetric) {
        if (healthMetricData.getMetricValue() != 0) {
            if (healthIsDegrading(healthMetricData)) {
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
            healthMetricData.setDegradingCounter(0);
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

    public static void setAutowireds(BaseNodeMonitorService baseNodeMonitorService, ITransactionHelper transactionHelper, IClusterService clusterService, TransactionIndexService transactionIndexService, IConfirmationService confirmationService, TrustChainConfirmationService trustChainConfirmationService, ITransactionService transactionService, IPropagationSubscriber propagationSubscriber, IWebSocketMessageService webSocketMessageService, INetworkService networkService, IReceiver receiver, IDatabaseConnector databaseConnector, IDBRecoveryService dbRecoveryService, RejectedTransactions rejectedTransactions, IPropagationPublisher propagationPublisher) {
        HealthMetric.monitorService = baseNodeMonitorService;
        HealthMetric.transactionHelper = transactionHelper;
        HealthMetric.clusterService = clusterService;
        HealthMetric.transactionIndexService = transactionIndexService;
        HealthMetric.confirmationService = confirmationService;
        HealthMetric.trustChainConfirmationService = trustChainConfirmationService;
        HealthMetric.transactionService = transactionService;
        HealthMetric.propagationSubscriber = propagationSubscriber;
        HealthMetric.webSocketMessageService = webSocketMessageService;
        HealthMetric.networkService = networkService;
        HealthMetric.receiver = receiver;
        HealthMetric.databaseConnector = databaseConnector;
        HealthMetric.dbRecoveryService = dbRecoveryService;
        HealthMetric.rejectedTransactions = rejectedTransactions;
        HealthMetric.propagationPublisher = propagationPublisher;
    }

    private void setHealthMetricBasePropertiesAndOutput(String _label, MetricClass _metricClass, long _warningThreshold, long _criticalThreshold, boolean _detailedLogs, HealthMetricOutputType _healthMetricOutputType) {
        label = _label;
        metricClass = _metricClass;
        warningThreshold = _warningThreshold;
        criticalThreshold = _criticalThreshold;
        detailedLogs = _detailedLogs;
        healthMetricOutputType = _healthMetricOutputType;
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

    @Override
    public HealthMetricData getHealthMetricData() {
        return monitorService.getHealthMetricData(this);
    }

}
