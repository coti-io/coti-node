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

import static io.coti.basenode.constants.BaseNodeHealthMetricConstants.*;


public enum HealthMetric implements IHealthMetric {

    TOTAL_TRANSACTIONS_DELTA(TOTAL_TRANSACTIONS_DELTA_LABEL, MetricClass.TRANSACTIONS_METRIC, 2, 3, true, HealthMetricOutputType.EXTERNAL) {
        @Override
        public void doSnapshot() {
            long totalTransactions = transactionHelper.getTotalTransactions();
            HealthMetricData healthMetricData = this.getHealthMetricData();
            healthMetricData.addValue(TOTAL_TRANSACTIONS_LABEL, HealthMetricOutputType.ALL, TOTAL_TRANSACTIONS_LABEL, totalTransactions);
            long totalTransactionsFromRecoveryServer = transactionHelper.getTotalNumberOfTransactionsFromRecovery();
            if (totalTransactionsFromRecoveryServer > 0) {
                healthMetricData.addValue(TOTAL_TRANSACTIONS_FROM_RECOVERY_LABEL, HealthMetricOutputType.EXTERNAL, TOTAL_TRANSACTIONS_FROM_RECOVERY_LABEL, totalTransactionsFromRecoveryServer);
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
            return "Discrepancy between the number of transactions declared by the recovery node and the total number of transactions recorded locally.";
        }
    },
    NUMBER_OF_TIMES_TCC_NOT_CHANGED(NUMBER_OF_TIMES_TCC_NOT_CHANGED_LABEL, MetricClass.TRANSACTIONS_METRIC, 5, 10, true, HealthMetricOutputType.EXTERNAL) {
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
            return "Number of times that Trust Chain Trust Score of oldest non Zero Spend transaction not changed";
        }
    },
    DSP_CONFIRMED_DELTA(DSP_CONFIRMED_LABEL_DELTA, MetricClass.TRANSACTIONS_METRIC, 2, 5, true, HealthMetricOutputType.EXTERNAL) {
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
            return "Discrepancy between the number of Total transactions and the number of DSP Confirmed";
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
            return "Total amount of transactions with both DSP Confirmed and Trust Chain Confirmed";
        }
    },
    INDEX_DELTA(INDEX_DELTA_LABEL, MetricClass.TRANSACTIONS_METRIC, 2, 4, true, HealthMetricOutputType.EXTERNAL) {
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
            return "Discrepancy between the number of total transactions and the index value. The highest contiguous index value should be one less than the total number of transactions";
        }
    },
    SOURCES_UPPER_BOUND(SOURCES_UPPER_BOUND_LABEL, MetricClass.TRANSACTIONS_METRIC, 24, 34, false, HealthMetricOutputType.EXTERNAL) {
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
            return "Upper aloud bound of total amount of transactions (including Zero spend transactions) that are sources";
        }
    },
    SOURCES_LOWER_BOUND(SOURCES_LOWER_BOUND_LABEL, MetricClass.TRANSACTIONS_METRIC, -8, -6, false, HealthMetricOutputType.EXTERNAL) {
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
            return "Lower aloud bound of total amount of transactions (including Zero spend transactions) that are sources";
        }
    },
    DCR_CONFIRMATION_QUEUE_SIZE(DCR_CONFIRMATION_QUEUE_SIZE_LABEL, MetricClass.QUEUE_METRIC, 100, 0, false, HealthMetricOutputType.EXTERNAL) {
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
            return "A number of transactions with DSP consensus that are waiting to be indexed in the expected sequential order.";
        }
    },
    TCC_CONFIRMATION_QUEUE(TCC_CONFIRMATION_QUEUE_LABEL, MetricClass.QUEUE_METRIC, 100, 0, false, HealthMetricOutputType.EXTERNAL) {
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
    WAITING_MISSING_TRANSACTION_INDEXES(WAITING_MISSING_TRANSACTION_INDEXES_LABEL, MetricClass.TRANSACTIONS_METRIC, 1, 5, false, HealthMetricOutputType.ALL) {
        public void doSnapshot() {
            baseDoSnapshot(this, (long) confirmationService.getWaitingMissingTransactionIndexesSize());
        }

        @Override
        public void calculateHealthMetric() {
            baseCalculateHealthCounterMetricState(this);
        }

        @Override
        public String getDescription() {
            return "A number of missing transactions that have yet to be indexed, either because of the expected sequential order or because of lack of DSP Consensus.";
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
            return "Total number of transactions that are considered postponed due to missing parents.";
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
    CONNECTED_TO_RECOVERY(CONNECTED_TO_RECOVERY_LABEL, MetricClass.TRANSACTIONS_METRIC, 1, 1, false, HealthMetricOutputType.EXTERNAL) {
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
    TRANSACTION_PROPAGATION_QUEUE(TRANSACTION_PROPAGATION_QUEUE_LABEL, MetricClass.QUEUE_METRIC, 64, 0, false, HealthMetricOutputType.EXTERNAL) {
        public void doSnapshot() {
            baseDoSnapshot(this, (long) propagationSubscriber.getMessageQueueSize(ZeroMQSubscriberQueue.TRANSACTION));
        }

        @Override
        public void calculateHealthMetric() {
            baseCalculateHealthCounterMetricState(this);
        }

        @Override
        public String getDescription() {
            return "ZeroMQ Subscriber Queue size for Transactions";
        }
    },
    PROPAGATION_SUBSCRIBER_TRANSACTIONS_STATE_QUEUE_SIZE(PROPAGATION_SUBSCRIBER_TRANSACTIONS_STATE_QUEUE_SIZE_LABEL, MetricClass.QUEUE_METRIC, 5, 0, false, HealthMetricOutputType.EXTERNAL) {
        public void doSnapshot() {
            baseDoSnapshot(this, (long) propagationSubscriber.getMessageQueueSize(ZeroMQSubscriberQueue.TRANSACTIONS_STATE));
        }

        @Override
        public void calculateHealthMetric() {
            calculateHealthValueMetricState(this);
        }

        @Override
        public String getDescription() {
            return "ZeroMQ Subscriber Queue size for Transactions State";
        }
    },
    PROPAGATION_SUBSCRIBER_NETWORK_QUEUE_SIZE(PROPAGATION_SUBSCRIBER_NETWORK_QUEUE_SIZE_LABEL, MetricClass.QUEUE_METRIC, 10, 0, false, HealthMetricOutputType.EXTERNAL) {
        public void doSnapshot() {
            baseDoSnapshot(this, (long) propagationSubscriber.getMessageQueueSize(ZeroMQSubscriberQueue.NETWORK));
        }

        @Override
        public void calculateHealthMetric() {
            calculateHealthValueMetricState(this);
        }

        @Override
        public String getDescription() {
            return "ZeroMQ Subscriber Queue size for Network State";
        }
    },
    PROPAGATION_SUBSCRIBER_ADDRESS_QUEUE_SIZE(PROPAGATION_SUBSCRIBER_ADDRESS_QUEUE_SIZE_LABEL, MetricClass.QUEUE_METRIC, 40, 0, false, HealthMetricOutputType.EXTERNAL) {
        public void doSnapshot() {
            baseDoSnapshot(this, (long) propagationSubscriber.getMessageQueueSize(ZeroMQSubscriberQueue.ADDRESS));
        }

        @Override
        public void calculateHealthMetric() {
            calculateHealthValueMetricState(this);

        }

        @Override
        public String getDescription() {
            return "ZeroMQ Subscriber Queue size for Address";
        }
    },
    PROPAGATION_SUBSCRIBER_TRANSACTION_QUEUE_SIZE(PROPAGATION_SUBSCRIBER_TRANSACTION_QUEUE_SIZE_LABEL, MetricClass.QUEUE_METRIC, 40, 0, false, HealthMetricOutputType.EXTERNAL) {
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
    PROPAGATION_SUBSCRIBER_HEARTBEAT_QUEUE_SIZE(PROPAGATION_SUBSCRIBER_HEARTBEAT_QUEUE_SIZE_LABEL, MetricClass.QUEUE_METRIC, 10, 0, false, HealthMetricOutputType.EXTERNAL) {
        public void doSnapshot() {
            baseDoSnapshot(this, (long) propagationSubscriber.getMessageQueueSize(ZeroMQSubscriberQueue.HEARTBEAT));
        }

        @Override
        public void calculateHealthMetric() {
            calculateHealthValueMetricState(this);
        }

        @Override
        public String getDescription() {
            return "ZeroMQ Subscriber Queue size for Heartbeat";
        }
    },
    ZERO_MQ_RECEIVER_QUEUE_SIZE(ZERO_MQ_RECEIVER_QUEUE_SIZE_LABEL, MetricClass.QUEUE_METRIC, 100, 0, false, HealthMetricOutputType.EXTERNAL) {
        public void doSnapshot() {
            baseDoSnapshot(this, (long) receiver.getQueueSize());
        }

        @Override
        public void calculateHealthMetric() {
            calculateHealthValueMetricState(this);
        }

        @Override
        public String getDescription() {
            return "ZeroMQ Receiver Queue size";
        }
    },
    PROPAGATION_PUBLISHER_QUEUE_SIZE(PROPAGATION_PUBLISHER_QUEUE_SIZE_LABEL, MetricClass.QUEUE_METRIC, 100, 0, false, HealthMetricOutputType.EXTERNAL) {
        public void doSnapshot() {
            baseDoSnapshot(this, (long) propagationPublisher.getQueueSize());
        }

        @Override
        public void calculateHealthMetric() {
            calculateHealthValueMetricState(this);
        }

        @Override
        public String getDescription() {
            return "ZeroMQ Publisher Queue size";
        }
    },
    LIVE_FILES_SIZE(LIVE_FILES_SIZE_LABEL, MetricClass.DATABASE_METRIC, 100, 0, false, HealthMetricOutputType.EXTERNAL) {
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
            return "The number of the live files";
        }
    },
    LAST_BACKUP_ELAPSED(LAST_BACKUP_ELAPSED_LABEL, MetricClass.BACKUP_METRIC, 3600, 0, false, HealthMetricOutputType.EXTERNAL) {
        public void doSnapshot() {
            if (newBackupExecuted()) {
                long backupStartedTime = dbRecoveryService.getBackupStartedTime();
                this.getHealthMetricData().addValue(BACKUP_STARTED_TIME_LABEL, HealthMetricOutputType.EXTERNAL, BACKUP_STARTED_TIME_LABEL, backupStartedTime);
                baseDoSnapshot(this, java.time.Instant.now().getEpochSecond() - backupStartedTime);
            }
        }

        @Override
        public void calculateHealthMetric() {
            if (newBackupExecuted()) {
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
    NUMBER_OF_LIVE_FILES_NOT_BACKED_UP(NUMBER_OF_LIVE_FILES_NOT_BACKED_UP_LABEL, MetricClass.BACKUP_METRIC, 1, 0, false, HealthMetricOutputType.EXTERNAL) {
        public void doSnapshot() {
            if (newBackupExecuted()) {
                long numberOfBackedFiles = dbRecoveryService.getLastBackupInfo().numberFiles();
                long numberOfLiveFiles = databaseConnector.getLiveFilesNames().size();
                this.getHealthMetricData().addValue(BACKED_UP_NUMBER_OF_FILES_LABEL, HealthMetricOutputType.EXTERNAL, BACKED_UP_NUMBER_OF_FILES_LABEL, numberOfBackedFiles);
                baseDoSnapshot(this, numberOfLiveFiles - numberOfBackedFiles);
            }
        }

        @Override
        public void calculateHealthMetric() {
            if (newBackupExecuted()) {
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
    BACKUP_SIZE(BACKUP_SIZE_LABEL, MetricClass.BACKUP_METRIC, 0, 0, false, HealthMetricOutputType.EXTERNAL) {
        public void doSnapshot() {
            if (newBackupExecuted()) {
                baseDoSnapshot(this, dbRecoveryService.getLastBackupInfo().size());
            }
        }

        @Override
        public void calculateHealthMetric() {
            monitorService.getHealthMetricData(this).setLastHealthState(BaseNodeMonitorService.HealthState.NA);
        }

        @Override
        public String getDescription() {
            return "The size of the last backup";
        }
    },
    BACKUP_ENTIRE_DURATION(BACKUP_ENTIRE_DURATION_LABEL, MetricClass.BACKUP_METRIC, 75, 180, false, HealthMetricOutputType.EXTERNAL) {
        public void doSnapshot() {
            if (newBackupExecuted()) {
                baseDoSnapshot(this, dbRecoveryService.getEntireDuration());
            }
        }

        @Override
        public void calculateHealthMetric() {
            if (newBackupExecuted()) {
                calculateHealthValueMetricState(this);
            } else {
                monitorService.getHealthMetricData(this).setLastHealthState(BaseNodeMonitorService.HealthState.NA);
            }
        }

        @Override
        public String getDescription() {
            return "The duration of the entire backup process";
        }
    },
    BACKUP_DURATION(BACKUP_DURATION_LABEL, MetricClass.BACKUP_METRIC, 45, 90, false, HealthMetricOutputType.EXTERNAL) {
        public void doSnapshot() {
            if (newBackupExecuted()) {
                baseDoSnapshot(this, dbRecoveryService.getBackupDuration());
            }
        }

        @Override
        public void calculateHealthMetric() {
            if (newBackupExecuted()) {
                calculateHealthValueMetricState(this);
            } else {
                monitorService.getHealthMetricData(this).setLastHealthState(BaseNodeMonitorService.HealthState.NA);
            }
        }

        @Override
        public String getDescription() {
            return "The duration for backup creation";
        }
    },
    BACKUP_UPLOAD_DURATION(BACKUP_UPLOAD_DURATION_LABEL, MetricClass.BACKUP_METRIC, 20, 60, false, HealthMetricOutputType.EXTERNAL) {
        public void doSnapshot() {
            if (newBackupExecuted()) {
                baseDoSnapshot(this, dbRecoveryService.getUploadDuration());
            }
        }

        @Override
        public void calculateHealthMetric() {
            if (newBackupExecuted()) {
                calculateHealthValueMetricState(this);
            } else {
                monitorService.getHealthMetricData(this).setLastHealthState(BaseNodeMonitorService.HealthState.NA);
            }
        }

        @Override
        public String getDescription() {
            return "The duration for backup upload";
        }
    },
    BACKUP_REMOVAL_DURATION(BACKUP_REMOVAL_DURATION_LABEL, MetricClass.BACKUP_METRIC, 10, 30, false, HealthMetricOutputType.EXTERNAL) {
        public void doSnapshot() {
            if (newBackupExecuted()) {
                baseDoSnapshot(this, dbRecoveryService.getRemovalDuration());
            }
        }

        @Override
        public void calculateHealthMetric() {
            if (newBackupExecuted()) {
                calculateHealthValueMetricState(this);
            } else {
                monitorService.getHealthMetricData(this).setLastHealthState(BaseNodeMonitorService.HealthState.NA);
            }
        }

        @Override
        public String getDescription() {
            return "The duration for backup removal";
        }
    },
    REJECTED_TRANSACTIONS(REJECTED_TRANSACTIONS_LABEL, MetricClass.TRANSACTIONS_METRIC, 30, 0, true, HealthMetricOutputType.EXTERNAL) {
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
            return "Amount of rejected transactions during the last 30 days";
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
    private final String label;
    @Getter
    private final boolean detailedLogs;
    @Getter
    private final HealthMetricOutputType healthMetricOutputType;
    @Getter
    private final MetricClass metricClass;
    @Getter
    private long warningThreshold;
    @Getter
    private long criticalThreshold;

    HealthMetric(String label, MetricClass metricClass, long warningThreshold, long criticalThreshold, boolean detailedLogs, HealthMetricOutputType healthMetricOutputType) {
        this.label = label;
        this.metricClass = metricClass;
        this.warningThreshold = warningThreshold;
        this.criticalThreshold = criticalThreshold;
        this.detailedLogs = detailedLogs;
        this.healthMetricOutputType = healthMetricOutputType;
    }

    private static void baseCalculateHealthCounterMetricState(HealthMetric healthMetric) {
        HealthMetricData healthMetricData = monitorService.getHealthMetricData(healthMetric);
        calculateHealthCounterMetricState(healthMetricData, healthMetric);
    }

    private static synchronized void baseDoSnapshot(HealthMetric healthMetric, Long metricValue) {
        monitorService.setMetricValue(healthMetric, metricValue);
        monitorService.setSnapshotTime(healthMetric, Instant.now());
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

    public static boolean isToAddExternalMetric(HealthMetricOutputType healthMetricOutputType) {
        return healthMetricOutputType.equals(HealthMetricOutputType.ALL) ||
                healthMetricOutputType.equals(HealthMetricOutputType.EXTERNAL);
    }

    public static boolean isToAddConsoleMetric(HealthMetricOutputType healthMetricOutputType) {
        return healthMetricOutputType.equals(HealthMetricOutputType.ALL) ||
                healthMetricOutputType.equals(HealthMetricOutputType.CONSOLE);
    }

    private static boolean newBackupExecuted() {
        return dbRecoveryService != null && dbRecoveryService.isBackup() && dbRecoveryService.getLastBackupInfo() != null;
    }

    @Override
    public HealthMetricData getHealthMetricData() {
        return monitorService.getHealthMetricData(this);
    }

    @Override
    public void setWarningThreshold(long l) {
        warningThreshold = l;
    }

    @Override
    public void setCriticalThreshold(long l) {
        criticalThreshold = l;
    }
}
