package io.coti.basenode.services;

import io.coti.basenode.communication.ZeroMQSubscriberQueue;
import io.coti.basenode.data.HealthMetricData;
import io.coti.basenode.data.HealthMetricOutputType;
import io.coti.basenode.data.HealthState;
import io.coti.basenode.data.MetricClass;
import io.coti.basenode.services.interfaces.IHealthMetric;
import io.coti.basenode.utilities.MemoryUtils;
import lombok.Getter;

import java.time.Instant;

import static io.coti.basenode.constants.BaseNodeHealthMetricConstants.*;
import static io.coti.basenode.services.BaseNodeServiceManager.*;

public enum HealthMetric implements IHealthMetric {

    TOTAL_TRANSACTIONS_DELTA(TOTAL_TRANSACTIONS_DELTA_LABEL, MetricClass.TRANSACTIONS_METRIC, 2, 5, true, HealthMetricOutputType.EXTERNAL) {
        @Override
        public void doSnapshot() {
            long totalTransactions = nodeTransactionHelper.getTotalTransactions();
            HealthMetricData healthMetricData = this.getHealthMetricData();
            healthMetricData.addValue(TOTAL_TRANSACTIONS_LABEL, HealthMetricOutputType.ALL, TOTAL_TRANSACTIONS_LABEL, totalTransactions);
            long totalTransactionsFromRecoveryServer = nodeTransactionHelper.getTotalNumberOfTransactionsFromRecovery();
            if (totalTransactionsFromRecoveryServer > 0) {
                healthMetricData.addValue(TOTAL_TRANSACTIONS_FROM_RECOVERY_LABEL, HealthMetricOutputType.EXTERNAL, TOTAL_TRANSACTIONS_FROM_RECOVERY_LABEL, totalTransactionsFromRecoveryServer);
                baseDoSnapshot(this, Math.abs(totalTransactionsFromRecoveryServer - totalTransactions));
            } else {
                baseDoSnapshot(this, (long) -1);
            }
        }

        @Override
        public void calculateHealthMetric() {
            if (monitorService.getHealthMetricData(this).getMetricValue() > -1) {
                baseCalculateHealthCounterMetricState(this);
            } else {
                monitorService.getHealthMetricData(this).setLastHealthState(HealthState.NA);
            }
        }

        @Override
        public String getDescription() {
            return "The discrepancy between the number of transactions stated by network vs. the number of transactions recorded in node.";
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
            return "The number of times that Trust Chain Trust Score of the oldest non ZeroSpend transaction in cluster has not changed.";
        }
    },
    DSP_CONFIRMED_DELTA(DSP_CONFIRMED_LABEL_DELTA, MetricClass.TRANSACTIONS_METRIC, 2, 5, true, HealthMetricOutputType.EXTERNAL) {
        public void doSnapshot() {
            long dspConfirmed = confirmationService.getDspConfirmed();
            long totalTransactions = nodeTransactionHelper.getTotalTransactions();
            this.getHealthMetricData().addValue(DSP_CONFIRMED_LABEL, HealthMetricOutputType.ALL, DSP_CONFIRMED_LABEL, dspConfirmed);
            baseDoSnapshot(this, totalTransactions - dspConfirmed);
        }

        @Override
        public void calculateHealthMetric() {
            baseCalculateHealthCounterMetricState(this);
        }

        @Override
        public String getDescription() {
            return "The discrepancy between the number of total transactions vs. the number of confirmed DSP transactions.";
        }
    },
    TOTAL_CONFIRMED(TOTAL_CONFIRMED_LABEL, MetricClass.TRANSACTIONS_METRIC, 0, 0, true, HealthMetricOutputType.ALL) {
        public void doSnapshot() {
            baseDoSnapshot(this, confirmationService.getTotalConfirmed());
        }

        @Override
        public void calculateHealthMetric() {
            HealthMetricData healthMetricData = monitorService.getHealthMetricData(this);
            healthMetricData.setLastHealthState(HealthState.NA);
        }

        @Override
        public String getDescription() {
            return "The total number of transactions that both DSP and Trust Chain confirmed.";
        }
    },
    INDEX_DELTA(INDEX_DELTA_LABEL, MetricClass.TRANSACTIONS_METRIC, 2, 4, true, HealthMetricOutputType.EXTERNAL) {
        public void doSnapshot() {
            long index = transactionIndexService.getLastTransactionIndexData().getIndex();
            this.getHealthMetricData().addValue(INDEX_LABEL, HealthMetricOutputType.ALL, INDEX_LABEL, index);
            baseDoSnapshot(this, (nodeTransactionHelper.getTotalTransactions() - index) - 1);
        }

        @Override
        public void calculateHealthMetric() {
            baseCalculateHealthCounterMetricState(this);
        }

        @Override
        public String getDescription() {
            return "The discrepancy between the total number of transactions vs. the highest index value. (Highest contiguous index value should be equal to the amount of transactions).";
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
            return "The upper bound of amount of transactions (including ZeroSpend) that are a source.";
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
            return "The lower bound of amount of transactions (including ZeroSpend) that are a source.";
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
            return "The number of DSP Consensus Results waiting for confirmation.";
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
            return "The number of confirmed DSP transactions waiting to be indexed in sequential order.";
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
            return "The number of transactions in Trust Chain Cluster waiting for confirmation.";
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
            return "The number of missing transactions (during startup) waiting to be indexed in sequential order.";
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
            return "The number of postponed transactions due to missing parents.";
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
            return "The number of websocket messages waiting in queue.";
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
            return "Percentage of used heap memory.";
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
            return "Percentage of used memory.";
        }
    },
    NOT_CONNECTED_TO_RECOVERY(NOT_CONNECTED_TO_RECOVERY_LABEL, MetricClass.TRANSACTIONS_METRIC, 1, 1, false, HealthMetricOutputType.EXTERNAL) {
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
            return "Indicates if node is connected to the recovery server.";
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
            return "The number of transaction state messages waiting in ZeroMQ for processing.";
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
            return "The number of network messages waiting in ZeroMQ for processing.";
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
            return "The number of address messages waiting in ZeroMQ for processing.";
        }
    },
    PROPAGATION_SUBSCRIBER_TRANSACTION_QUEUE_SIZE(PROPAGATION_SUBSCRIBER_TRANSACTION_QUEUE_SIZE_LABEL, MetricClass.QUEUE_METRIC, 100, 0, false, HealthMetricOutputType.EXTERNAL) {
        public void doSnapshot() {
            baseDoSnapshot(this, (long) propagationSubscriber.getMessageQueueSize(ZeroMQSubscriberQueue.TRANSACTION));
        }

        @Override
        public void calculateHealthMetric() {
            calculateHealthValueMetricState(this);
        }

        @Override
        public String getDescription() {
            return "The number of transactions messages waiting in ZeroMQ for processing.";
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
            return "The number of heartbeat messages waiting in ZeroMQ for processing.";
        }
    },
    ZERO_MQ_RECEIVER_QUEUE_SIZE(ZERO_MQ_RECEIVER_QUEUE_SIZE_LABEL, MetricClass.QUEUE_METRIC, 100, 0, false, HealthMetricOutputType.EXTERNAL) {
        public void doSnapshot() {
            baseDoSnapshot(this, (long) zeroMQReceiver.getQueueSize());
        }

        @Override
        public void calculateHealthMetric() {
            calculateHealthValueMetricState(this);
        }

        @Override
        public String getDescription() {
            return "The number of direct messages received via ZeroMQ.";
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
            return "The number of messages waiting in ZeroMQ for propagation.";
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
                monitorService.getHealthMetricData(this).setLastHealthState(HealthState.NA);
            }
        }

        @Override
        public String getDescription() {
            return "The number of RocksDB live files.";
        }
    },
    LAST_BACKUP_ELAPSED(LAST_BACKUP_ELAPSED_LABEL, MetricClass.BACKUP_METRIC, 3600, 0, false, HealthMetricOutputType.EXTERNAL) {
        public void doSnapshot() {
            if (newBackupExecuted()) {
                long backupStartedTime = dbRecoveryService.getLastBackupStartedTime();
                this.getHealthMetricData().addValue(BACKUP_STARTED_TIME_LABEL, HealthMetricOutputType.EXTERNAL, BACKUP_STARTED_TIME_LABEL, backupStartedTime);
                baseDoSnapshot(this, java.time.Instant.now().getEpochSecond() - backupStartedTime);
            }
        }

        @Override
        public void calculateHealthMetric() {
            if (newBackupExecuted()) {
                calculateHealthValueMetricState(this);
            } else {
                monitorService.getHealthMetricData(this).setLastHealthState(HealthState.NA);
            }
        }

        @Override
        public String getDescription() {
            return "The time that elapsed since the last successful backup (seconds).";
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
                monitorService.getHealthMetricData(this).setLastHealthState(HealthState.NA);
            }
        }

        @Override
        public String getDescription() {
            return "The number of RocksDB live files not backed up.";
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
            monitorService.getHealthMetricData(this).setLastHealthState(HealthState.NA);
        }

        @Override
        public String getDescription() {
            return "The size of the last RocksDB backup (bytes).";
        }
    },
    BACKUP_ENTIRE_DURATION(BACKUP_ENTIRE_DURATION_LABEL, MetricClass.BACKUP_METRIC, 75, 180, false, HealthMetricOutputType.EXTERNAL) {
        public void doSnapshot() {
            if (newBackupExecuted()) {
                baseDoSnapshot(this, dbRecoveryService.getLastEntireDuration());
            }
        }

        @Override
        public void calculateHealthMetric() {
            if (newBackupExecuted()) {
                calculateHealthValueMetricState(this);
            } else {
                monitorService.getHealthMetricData(this).setLastHealthState(HealthState.NA);
            }
        }

        @Override
        public String getDescription() {
            return "The duration of the entire backup (seconds).";
        }
    },
    BACKUP_DURATION(BACKUP_DURATION_LABEL, MetricClass.BACKUP_METRIC, 45, 90, false, HealthMetricOutputType.EXTERNAL) {
        public void doSnapshot() {
            if (newBackupExecuted()) {
                baseDoSnapshot(this, dbRecoveryService.getLastBackupDuration());
            }
        }

        @Override
        public void calculateHealthMetric() {
            if (newBackupExecuted()) {
                calculateHealthValueMetricState(this);
            } else {
                monitorService.getHealthMetricData(this).setLastHealthState(HealthState.NA);
            }
        }

        @Override
        public String getDescription() {
            return "The duration of RocksDB backup (seconds).";
        }
    },
    BACKUP_UPLOAD_DURATION(BACKUP_UPLOAD_DURATION_LABEL, MetricClass.BACKUP_METRIC, 20, 60, false, HealthMetricOutputType.EXTERNAL) {
        public void doSnapshot() {
            if (newBackupExecuted()) {
                baseDoSnapshot(this, dbRecoveryService.getLastUploadDuration());
            }
        }

        @Override
        public void calculateHealthMetric() {
            if (newBackupExecuted()) {
                calculateHealthValueMetricState(this);
            } else {
                monitorService.getHealthMetricData(this).setLastHealthState(HealthState.NA);
            }
        }

        @Override
        public String getDescription() {
            return "The duration of backup upload (seconds).";
        }
    },
    BACKUP_REMOVAL_DURATION(BACKUP_REMOVAL_DURATION_LABEL, MetricClass.BACKUP_METRIC, 10, 30, false, HealthMetricOutputType.EXTERNAL) {
        public void doSnapshot() {
            if (newBackupExecuted()) {
                baseDoSnapshot(this, dbRecoveryService.getLastRemovalDuration());
            }
        }

        @Override
        public void calculateHealthMetric() {
            if (newBackupExecuted()) {
                calculateHealthValueMetricState(this);
            } else {
                monitorService.getHealthMetricData(this).setLastHealthState(HealthState.NA);
            }
        }

        @Override
        public String getDescription() {
            return "The duration of older backup removal (seconds).";
        }
    },
    REJECTED_TRANSACTIONS(REJECTED_TRANSACTIONS_LABEL, MetricClass.TRANSACTIONS_METRIC, 1, 3, false, HealthMetricOutputType.EXTERNAL) {
        @Override
        public void doSnapshot() {
            baseDoSnapshot(this, rejectedTransactions.size());
        }

        @Override
        public void calculateHealthMetric() {
            HealthMetricData healthMetricData = monitorService.getHealthMetricData(this);
            if (healthIsDegrading(healthMetricData)) {
                healthMetricData.increaseDegradingCounter();
            } else {
                healthMetricData.setDegradingCounter(0);
            }
            if (healthMetricData.getDegradingCounter() >= healthMetricData.getCriticalThreshold() && healthMetricData.getCriticalThreshold() >= healthMetricData.getWarningThreshold()) {
                healthMetricData.setLastHealthState(HealthState.CRITICAL);
            } else if (healthMetricData.getDegradingCounter() >= healthMetricData.getWarningThreshold()) {
                healthMetricData.setLastHealthState(HealthState.WARNING);
            } else {
                healthMetricData.setLastHealthState(HealthState.NORMAL);
            }
        }

        @Override
        public String getDescription() {
            return "The number of rejected transactions during a period (days).";
        }
    };

    @Getter
    private final String label;
    @Getter
    private final boolean detailedLogs;
    @Getter
    private final HealthMetricOutputType healthMetricOutputType;
    @Getter
    private final MetricClass metricClass;
    @Getter
    private final long defaultWarningThreshold;
    @Getter
    private final long defaultCriticalThreshold;

    HealthMetric(String label, MetricClass metricClass, long defaultWarningThreshold, long defaultCriticalThreshold, boolean detailedLogs, HealthMetricOutputType healthMetricOutputType) {
        this.label = label;
        this.metricClass = metricClass;
        this.defaultWarningThreshold = defaultWarningThreshold;
        this.defaultCriticalThreshold = defaultCriticalThreshold;
        this.detailedLogs = detailedLogs;
        this.healthMetricOutputType = healthMetricOutputType;
    }

    private static void baseCalculateHealthCounterMetricState(HealthMetric healthMetric) {
        HealthMetricData healthMetricData = monitorService.getHealthMetricData(healthMetric);
        calculateHealthCounterMetricState(healthMetricData);
    }

    private static synchronized void baseDoSnapshot(HealthMetric healthMetric, Long metricValue) {
        monitorService.setMetricValue(healthMetric, metricValue);
        monitorService.setSnapshotTime(healthMetric, Instant.now());
    }

    private static boolean healthIsDegrading(HealthMetricData healthMetricData) {
        return Math.abs(healthMetricData.getMetricValue()) > Math.abs(healthMetricData.getPreviousMetricValue());
    }

    private static void calculateHealthCounterMetricState(HealthMetricData healthMetricData) {
        if (healthMetricData.getMetricValue() != 0) {
            if (healthIsDegrading(healthMetricData)) {
                healthMetricData.increaseDegradingCounter();
            }
            if (healthMetricData.getDegradingCounter() >= healthMetricData.getCriticalThreshold() && healthMetricData.getCriticalThreshold() >= healthMetricData.getWarningThreshold()) {
                healthMetricData.setLastHealthState(HealthState.CRITICAL);
            } else if (healthMetricData.getDegradingCounter() >= healthMetricData.getWarningThreshold()) {
                healthMetricData.setLastHealthState(HealthState.WARNING);
            } else if (HealthState.NA.equals(healthMetricData.getLastHealthState())) {
                healthMetricData.setLastHealthState(HealthState.NORMAL);
            }
        } else {
            healthMetricData.setDegradingCounter(0);
            healthMetricData.setLastHealthState(HealthState.NORMAL);
        }
    }

    private static void calculateHealthValueMetricState(HealthMetric healthMetric) {
        HealthMetricData healthMetricData = monitorService.getHealthMetricData(healthMetric);
        long currentMetricValue = healthMetricData.getMetricValue();
        if (currentMetricValue >= healthMetricData.getCriticalThreshold() && healthMetricData.getCriticalThreshold() >= healthMetricData.getWarningThreshold()) {
            healthMetricData.setLastHealthState(HealthState.CRITICAL);
        } else if (currentMetricValue >= healthMetricData.getWarningThreshold()) {
            healthMetricData.setLastHealthState(HealthState.WARNING);
        } else {
            healthMetricData.setLastHealthState(HealthState.NORMAL);
        }
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
        return dbRecoveryService != null && dbRecoveryService.isBackup() && dbRecoveryService.getLastBackupInfo() != null
                && !dbRecoveryService.getBackupInProgress().get();
    }

    @Override
    public HealthMetricData getHealthMetricData() {
        return monitorService.getHealthMetricData(this);
    }

}
