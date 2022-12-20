package io.coti.basenode.services;

import io.coti.basenode.communication.ZeroMQSubscriberQueue;
import io.coti.basenode.communication.interfaces.IPropagationPublisher;
import io.coti.basenode.communication.interfaces.IPropagationSubscriber;
import io.coti.basenode.communication.interfaces.IReceiver;
import io.coti.basenode.data.HealthMetricData;
import io.coti.basenode.data.MetricType;
import io.coti.basenode.database.interfaces.IDatabaseConnector;
import io.coti.basenode.services.interfaces.*;
import io.coti.basenode.utilities.MemoryUtils;
import org.springframework.boot.actuate.health.Health;

import java.time.Instant;
import java.util.Arrays;
import java.util.Map;

import static io.coti.basenode.constants.BaseNodeHealthMetricConstants.*;

public enum HealthMetric implements IHealthMetric {


    TOTAL_TRANSACTIONS(TOTAL_TRANSACTIONS_LABEL, true, MetricType.TRANSACTIONS_METRIC, 0, 0, true) {
        @Override
        public void doSnapshot() {
            monitorService.setLastMetricValue(this, transactionHelper.getTotalTransactions());
            monitorService.getHealthMetricData(this).setSpecificLastMetricValue(SNAPSHOT_TOTAL_TRANSACTIONS_FROM_RECOVERY, transactionHelper.getTotalNumberOfTransactionsFromRecovery());
            monitorService.setSnapshotTime(this, String.valueOf(Instant.now().toEpochMilli()));
        }

        @Override
        public void calculateHealthMetric() {
            HealthMetricData healthMetricData = monitorService.getHealthMetricData(this);
            long conditionValue = healthMetricData.getSpecificLastMetricValue(SNAPSHOT_TOTAL_TRANSACTIONS_FROM_RECOVERY) - healthMetricData.getLastMetricValue();
            healthMetricData.setLastConditionValue(conditionValue);
            calculateHealthCounterMetricState(healthMetricData, this, true);
        }
    },
    SOURCES_UPPER_BOUND(SOURCES_UPPER_BOUND_LABEL, false, MetricType.TRANSACTIONS_METRIC, 24, 34, false) {
        @Override
        public void doSnapshot() {
            monitorService.setLastMetricValue(this, clusterService.getTotalSources());
            monitorService.setSnapshotTime(this, String.valueOf(Instant.now().toEpochMilli()));
        }

        @Override
        public void calculateHealthMetric() {
            HealthMetricData healthMetricData = monitorService.getHealthMetricData(this);
            healthMetricData.setLastConditionValue(healthMetricData.getLastMetricValue());
            calculateHealthValueMetricState(healthMetricData, this);
        }
    },
    SOURCES_LOWER_BOUND(SOURCES_LOWER_BOUND_LABEL, false, MetricType.TRANSACTIONS_METRIC, -8, -6, false) {
        @Override
        public void doSnapshot() {
            monitorService.setLastMetricValue(this, clusterService.getTotalSources());
            monitorService.setSnapshotTime(this, String.valueOf(Instant.now().toEpochMilli()));
        }

        @Override
        public void calculateHealthMetric() {
            HealthMetricData healthMetricData = monitorService.getHealthMetricData(this);
            healthMetricData.setLastConditionValue(-healthMetricData.getLastMetricValue());
            calculateHealthValueMetricState(healthMetricData, this);
        }
    },
    INDEX(INDEX_LABEL, true, MetricType.TRANSACTIONS_METRIC, 2, 0, true) {
        public void doSnapshot() {
            monitorService.setLastMetricValue(this, transactionIndexService.getLastTransactionIndexData().getIndex());
            monitorService.getHealthMetricData(this).setSpecificLastMetricValue(SNAPSHOT_TOTAL_TRANSACTIONS_FROM_LOCAL, transactionHelper.getTotalNumberOfTransactionsFromLocal());
            monitorService.setSnapshotTime(this, String.valueOf(Instant.now().toEpochMilli()));
        }

        @Override
        public void calculateHealthMetric() {
            HealthMetricData healthMetricData = monitorService.getHealthMetricData(this);
            long conditionValue = healthMetricData.getSpecificLastMetricValue(SNAPSHOT_TOTAL_TRANSACTIONS_FROM_LOCAL) - healthMetricData.getLastMetricValue() - 1;
            healthMetricData.setLastConditionValue(conditionValue);
            calculateHealthCounterMetricState(healthMetricData, this, true);
        }
    },
    WAITING_DSP_CONSENSUS_RESULTS_CONFIRMED(WAITING_DSP_CONSENSUS_RESULTS_CONFIRMED_LABEL, true, MetricType.TRANSACTIONS_METRIC, 1, 5, false) {
        public void doSnapshot() {
            monitorService.setLastMetricValue(this, confirmationService.getWaitingDspConsensusResultsMapSize());
            monitorService.setSnapshotTime(this, String.valueOf(Instant.now().toEpochMilli()));
        }

        @Override
        public void calculateHealthMetric() {
            HealthMetricData healthMetricData = monitorService.getHealthMetricData(this);
            healthMetricData.setLastConditionValue(healthMetricData.getLastMetricValue());
            calculateHealthCounterMetricState(healthMetricData, this, true);
        }
    },
    DSP_CONFIRMED(DSP_CONFIRMED_LABEL, true, MetricType.TRANSACTIONS_METRIC, 2, 5, true) {
        public void doSnapshot() {
            HealthMetricData healthMetricData = monitorService.getHealthMetricData(this);
            monitorService.setLastMetricValue(this, confirmationService.getDspConfirmed());
            monitorService.getHealthMetricData(this).setSpecificLastMetricValue(SNAPSHOT_DSP_CONFIRMED, healthMetricData.getLastMetricValue());
            monitorService.setSnapshotTime(this, String.valueOf(Instant.now().toEpochMilli()));
            healthMetricData.setSpecificLastMetricValue(SNAPSHOT_PREVIOUS_DSP_CONFIRMED, healthMetricData.getSpecificLastMetricValue(SNAPSHOT_DSP_CONFIRMED));
            monitorService.getHealthMetricData(this).setSpecificLastMetricValue(SNAPSHOT_TOTAL_TRANSACTIONS_FROM_LOCAL, transactionHelper.getTotalTransactions());
        }

        @Override
        public void calculateHealthMetric() {
            HealthMetricData healthMetricData = monitorService.getHealthMetricData(this);
            if (healthMetricData.getSpecificLastMetricValue(SNAPSHOT_TOTAL_TRANSACTIONS_FROM_LOCAL).equals(healthMetricData.getSpecificLastMetricValue(SNAPSHOT_DSP_CONFIRMED))
                    || !healthMetricData.getSpecificLastMetricValue(SNAPSHOT_DSP_CONFIRMED).equals(healthMetricData.getSpecificLastMetricValue(SNAPSHOT_PREVIOUS_DSP_CONFIRMED))) {
                healthMetricData.setLastConditionValue(0);
            } else {
                healthMetricData.setLastConditionValue(1);
            }
            calculateHealthCounterMetricState(healthMetricData, this, true);
        }
    },
    TOTAL_CONFIRMED(TOTAL_CONFIRMED_LABEL, true, MetricType.TRANSACTIONS_METRIC, 2, 5, true) {
        public void doSnapshot() {
            HealthMetricData healthMetricData = monitorService.getHealthMetricData(this);
            monitorService.setLastMetricValue(this, confirmationService.getTotalConfirmed());
            monitorService.getHealthMetricData(this).setSpecificLastMetricValue(SNAPSHOT_TOTAL_CONFIRMED, healthMetricData.getLastMetricValue());
            monitorService.setSnapshotTime(this, String.valueOf(Instant.now().toEpochMilli()));
            healthMetricData.setSpecificLastMetricValue(SNAPSHOT_PREVIOUS_TOTAL_CONFIRMED, healthMetricData.getSpecificLastMetricValue(SNAPSHOT_TOTAL_CONFIRMED));
            monitorService.getHealthMetricData(this).setSpecificLastMetricValue(SNAPSHOT_TCC_CONFIRMED, confirmationService.getTrustChainConfirmed());
        }

        @Override
        public void calculateHealthMetric() {
            HealthMetricData healthMetricData = monitorService.getHealthMetricData(this);
            if (healthMetricData.getSpecificLastMetricValue(SNAPSHOT_TCC_CONFIRMED).equals(healthMetricData.getSpecificLastMetricValue(SNAPSHOT_TOTAL_CONFIRMED))
                    || !healthMetricData.getSpecificLastMetricValue(SNAPSHOT_TOTAL_CONFIRMED).equals(healthMetricData.getSpecificLastMetricValue(SNAPSHOT_PREVIOUS_TOTAL_CONFIRMED))) {
                healthMetricData.setLastConditionValue(0);
            } else {
                healthMetricData.setLastConditionValue(1);
            }
            calculateHealthCounterMetricState(healthMetricData, this, true);
        }
    },
    TRUST_CHAIN_CONFIRMED(TRUST_CHAIN_CONFIRMED_LABEL, true, MetricType.TRANSACTIONS_METRIC, 5, 10, true) {
        public void doSnapshot() {
            HealthMetricData healthMetricData = monitorService.getHealthMetricData(this);
            monitorService.setLastMetricValue(this, confirmationService.getTrustChainConfirmed());
            int tccOutsideNormalCounter = trustChainConfirmationService.getTccOutsideNormalCounter();
            monitorService.setSnapshotTime(this, String.valueOf(Instant.now().toEpochMilli()));
            healthMetricData.setLastCounter(tccOutsideNormalCounter);
        }

        @Override
        public void calculateHealthMetric() {
            HealthMetricData healthMetricData = monitorService.getHealthMetricData(this);
            healthMetricData.setLastConditionValue(healthMetricData.getLastCounter());
            calculateHealthCounterMetricState(healthMetricData, this, false);
        }
    },
    WAITING_MISSING_TRANSACTION_INDEXES(WAITING_MISSING_TRANSACTION_INDEXES_LABEL, true, MetricType.TRANSACTIONS_METRIC, 1, 1, false) {
        public void doSnapshot() {
            long waitingMissingTransactionIndexesSize = confirmationService.getWaitingMissingTransactionIndexesSize();
            monitorService.setSnapshotTime(this, String.valueOf(Instant.now().toEpochMilli()));
            monitorService.setLastMetricValue(this, waitingMissingTransactionIndexesSize);
        }

        @Override
        public void calculateHealthMetric() {
            HealthMetricData healthMetricData = monitorService.getHealthMetricData(this);
            healthMetricData.setLastConditionValue(healthMetricData.getLastMetricValue());
            calculateHealthCounterMetricState(healthMetricData, this, true);
        }
    },
    TOTAL_POSTPONED_TRANSACTIONS(TOTAL_POSTPONED_TRANSACTIONS_LABEL, true, MetricType.TRANSACTIONS_METRIC, 2, 4, false) {
        public void doSnapshot() {
            int totalPostponedTransactions = transactionService.totalPostponedTransactions();
            monitorService.setSnapshotTime(this, String.valueOf(Instant.now().toEpochMilli()));
            monitorService.setLastMetricValue(this, totalPostponedTransactions);
        }

        @Override
        public void calculateHealthMetric() {
            HealthMetricData healthMetricData = monitorService.getHealthMetricData(this);
            healthMetricData.setLastConditionValue(healthMetricData.getLastMetricValue());
            calculateHealthCounterMetricState(healthMetricData, this, true);
        }
    },
    PROPAGATION_QUEUE(PROPAGATION_QUEUE_LABEL, false, MetricType.NA, 64, 0, false) {
        public void doSnapshot() {
            int messageQueueSize = propagationSubscriber.getMessageQueueSize(ZeroMQSubscriberQueue.TRANSACTION);
            monitorService.setSnapshotTime(this, String.valueOf(Instant.now().toEpochMilli()));
            monitorService.setLastMetricValue(this, messageQueueSize);
        }

        @Override
        public void calculateHealthMetric() {
            HealthMetricData healthMetricData = monitorService.getHealthMetricData(this);
            healthMetricData.setLastConditionValue(healthMetricData.getLastMetricValue());
            HealthMetric.calculateHealthValueMetricState(healthMetricData, this);
        }
    },
    WEB_SOCKET_MESSAGES_QUEUE_LENGTH(WEB_SOCKET_MESSAGES_QUEUE_LENGTH_LABEL, false, MetricType.QUEUE_METRIC, 100, 1000, false) {
        public void doSnapshot() {
            int messageQueueSize = webSocketMessageService.getMessageQueueSize();
            monitorService.setSnapshotTime(this, String.valueOf(Instant.now().toEpochMilli()));
            monitorService.setLastMetricValue(this, messageQueueSize);
        }

        @Override
        public void calculateHealthMetric() {
            HealthMetricData healthMetricData = monitorService.getHealthMetricData(this);
            healthMetricData.setLastConditionValue(healthMetricData.getLastMetricValue());
            HealthMetric.calculateHealthValueMetricState(healthMetricData, this);
        }
    },
    DCR_CONFIRMATION_QUEUE_SIZE(DCR_CONFIRMATION_QUEUE_SIZE_LABEL, false, MetricType.QUEUE_METRIC, 100, 0, false) {
        public void doSnapshot() {
            int queueSize = confirmationService.getDcrConfirmationQueueSize();
            monitorService.setSnapshotTime(this, String.valueOf(Instant.now().toEpochMilli()));
            monitorService.setLastMetricValue(this, queueSize);
        }

        @Override
        public void calculateHealthMetric() {
            HealthMetricData healthMetricData = monitorService.getHealthMetricData(this);
            healthMetricData.setLastConditionValue(healthMetricData.getLastMetricValue());
            HealthMetric.calculateHealthValueMetricState(healthMetricData, this);
        }
    },
    TCC_CONFIRMATION_QUEUE_SIZE(TCC_CONFIRMATION_QUEUE_SIZE_LABEL, false, MetricType.QUEUE_METRIC, 100, 0, false) {
        public void doSnapshot() {
            int queueSize = confirmationService.getTccConfirmationQueueSize();
            monitorService.setSnapshotTime(this, String.valueOf(Instant.now().toEpochMilli()));
            monitorService.setLastMetricValue(this, queueSize);
        }

        @Override
        public void calculateHealthMetric() {
            HealthMetricData healthMetricData = monitorService.getHealthMetricData(this);
            healthMetricData.setLastConditionValue(healthMetricData.getLastMetricValue());
            HealthMetric.calculateHealthValueMetricState(healthMetricData, this);
        }
    },
    PERCENTAGE_USED_HEAP_MEMORY(PERCENTAGE_USED_HEAP_MEMORY_LABEL, false, MetricType.NA, 95, 98, false) {
        public void doSnapshot() {
            double percentageUsedHeap = MemoryUtils.getPercentageUsedHeap();
            monitorService.setSnapshotTime(this, String.valueOf(Instant.now().toEpochMilli()));
            monitorService.setLastMetricValue(this, (long) percentageUsedHeap);
        }

        @Override
        public void calculateHealthMetric() {
            HealthMetricData healthMetricData = monitorService.getHealthMetricData(this);
            healthMetricData.setLastConditionValue(healthMetricData.getLastMetricValue());
            HealthMetric.calculateHealthValueMetricState(healthMetricData, this);
        }
    },
    PERCENTAGE_USED_MEMORY(PERCENTAGE_USED_MEMORY_LABEL, false, MetricType.NA, 85, 95, false) {
        public void doSnapshot() {
            double percentageUsed = MemoryUtils.getPercentageUsed();
            monitorService.setSnapshotTime(this, String.valueOf(Instant.now().toEpochMilli()));
            monitorService.setLastMetricValue(this, (long) percentageUsed);
        }

        @Override
        public void calculateHealthMetric() {
            HealthMetricData healthMetricData = monitorService.getHealthMetricData(this);
            healthMetricData.setLastConditionValue(healthMetricData.getLastMetricValue());
            HealthMetric.calculateHealthValueMetricState(healthMetricData, this);
        }
    },
    CONNECTED_TO_RECOVERY(CONNECTED_TO_RECOVERY_LABEL, true, MetricType.TRANSACTIONS_METRIC, 1, 1, false) {
        public void doSnapshot() {
            int notConnectedToRecovery = !networkService.isConnectedToRecovery() ? 1 : 0;
            monitorService.setSnapshotTime(this, String.valueOf(Instant.now().toEpochMilli()));
            monitorService.setLastMetricValue(this, notConnectedToRecovery);
        }

        @Override
        public void calculateHealthMetric() {
            HealthMetricData healthMetricData = monitorService.getHealthMetricData(this);
            healthMetricData.setLastConditionValue(healthMetricData.getLastMetricValue());
            calculateHealthCounterMetricState(healthMetricData, this, true);
        }
    },
    PROPAGATION_SUBSCRIBER_TRANSACTIONS_STATE_QUEUE_SIZE(PROPAGATION_SUBSCRIBER_TRANSACTIONS_STATE_QUEUE_SIZE_LABEL, false, MetricType.QUEUE_METRIC, 5, 0, false) {
        public void doSnapshot() {
            String queueName = ZeroMQSubscriberQueue.TRANSACTIONS_STATE.name();
            long queueSize = Integer.parseInt(propagationSubscriber.getQueueSizeMap().get(queueName));
            monitorService.setLastMetricValue(this, queueSize);
            monitorService.setSnapshotTime(this, String.valueOf(Instant.now().toEpochMilli()));
        }

        @Override
        public void calculateHealthMetric() {
            HealthMetricData healthMetricData = monitorService.getHealthMetricData(this);
            healthMetricData.setLastConditionValue(healthMetricData.getLastMetricValue());
            HealthMetric.calculateHealthValueMetricState(healthMetricData, this);
        }
    },
    PROPAGATION_SUBSCRIBER_NETWORK_QUEUE_SIZE(PROPAGATION_SUBSCRIBER_NETWORK_QUEUE_SIZE_LABEL, false, MetricType.QUEUE_METRIC, 10, 0, false) {
        public void doSnapshot() {
            String queueName = ZeroMQSubscriberQueue.NETWORK.name();
            long queueSize = Integer.parseInt(propagationSubscriber.getQueueSizeMap().get(queueName));
            monitorService.setLastMetricValue(this, queueSize);
            monitorService.setSnapshotTime(this, String.valueOf(Instant.now().toEpochMilli()));
        }

        @Override
        public void calculateHealthMetric() {
            HealthMetricData healthMetricData = monitorService.getHealthMetricData(this);
            healthMetricData.setLastConditionValue(healthMetricData.getLastMetricValue());
            HealthMetric.calculateHealthValueMetricState(healthMetricData, this);
        }
    },
    PROPAGATION_SUBSCRIBER_ADDRESS_QUEUE_SIZE(PROPAGATION_SUBSCRIBER_ADDRESS_QUEUE_SIZE_LABEL, false, MetricType.QUEUE_METRIC, 40, 0, false) {
        public void doSnapshot() {
            String queueName = ZeroMQSubscriberQueue.ADDRESS.name();
            long queueSize = Integer.parseInt(propagationSubscriber.getQueueSizeMap().get(queueName));
            monitorService.setLastMetricValue(this, queueSize);
            monitorService.setSnapshotTime(this, String.valueOf(Instant.now().toEpochMilli()));
        }

        @Override
        public void calculateHealthMetric() {
            HealthMetricData healthMetricData = monitorService.getHealthMetricData(this);
            healthMetricData.setLastConditionValue(healthMetricData.getLastMetricValue());
            HealthMetric.calculateHealthValueMetricState(healthMetricData, this);
        }
    },
    PROPAGATION_SUBSCRIBER_TRANSACTION_QUEUE_SIZE(PROPAGATION_SUBSCRIBER_TRANSACTION_QUEUE_SIZE_LABEL, false, MetricType.QUEUE_METRIC, 40, 0, false) {
        public void doSnapshot() {
            String queueName = ZeroMQSubscriberQueue.TRANSACTION.name();
            long queueSize = Integer.parseInt(propagationSubscriber.getQueueSizeMap().get(queueName));
            monitorService.setLastMetricValue(this, queueSize);
            monitorService.setSnapshotTime(this, String.valueOf(Instant.now().toEpochMilli()));
        }

        @Override
        public void calculateHealthMetric() {
            HealthMetricData healthMetricData = monitorService.getHealthMetricData(this);
            healthMetricData.setLastConditionValue(healthMetricData.getLastMetricValue());
            HealthMetric.calculateHealthValueMetricState(healthMetricData, this);
        }
    },
    PROPAGATION_SUBSCRIBER_HEARTBEAT_QUEUE_SIZE(PROPAGATION_SUBSCRIBER_HEARTBEAT_QUEUE_SIZE_LABEL, false, MetricType.QUEUE_METRIC, 10, 0, false) {
        public void doSnapshot() {
            String queueName = ZeroMQSubscriberQueue.HEARTBEAT.name();
            long queueSize = Integer.parseInt(propagationSubscriber.getQueueSizeMap().get(queueName));
            monitorService.setLastMetricValue(this, queueSize);
            monitorService.setSnapshotTime(this, String.valueOf(Instant.now().toEpochMilli()));
        }

        @Override
        public void calculateHealthMetric() {
            HealthMetricData healthMetricData = monitorService.getHealthMetricData(this);
            healthMetricData.setLastConditionValue(healthMetricData.getLastMetricValue());
            HealthMetric.calculateHealthValueMetricState(healthMetricData, this);
        }
    },
    ZERO_MQ_RECEIVER_QUEUE_SIZE(ZERO_MQ_RECEIVER_QUEUE_SIZE_LABEL, false, MetricType.QUEUE_METRIC, 100, 0, false) {
        public void doSnapshot() {
            long queueSize = receiver.getQueueSize();
            monitorService.setLastMetricValue(this, queueSize);
            monitorService.setSnapshotTime(this, String.valueOf(Instant.now().toEpochMilli()));
        }

        @Override
        public void calculateHealthMetric() {
            HealthMetricData healthMetricData = monitorService.getHealthMetricData(this);
            healthMetricData.setLastConditionValue(healthMetricData.getLastMetricValue());
            HealthMetric.calculateHealthValueMetricState(healthMetricData, this);
        }
    },
    PROPAGATION_PUBLISHER_QUEUE_SIZE(PROPAGATION_PUBLISHER_QUEUE_SIZE_LABEL, false, MetricType.QUEUE_METRIC, 100, 0, false) {
        public void doSnapshot() {
            long queueSize = propagationPublisher.getQueueSize();
            monitorService.setLastMetricValue(this, queueSize);
            monitorService.setSnapshotTime(this, String.valueOf(Instant.now().toEpochMilli()));
        }

        @Override
        public void calculateHealthMetric() {
            HealthMetricData healthMetricData = monitorService.getHealthMetricData(this);
            healthMetricData.setLastConditionValue(healthMetricData.getLastMetricValue());
            HealthMetric.calculateHealthValueMetricState(healthMetricData, this);
        }
    },
    LIVE_FILES_SIZE(LIVE_FILES_SIZE_LABEL, false, MetricType.DATABASE_METRIC, 100, 0, false) {
        public void doSnapshot() {
            long liveFilesAmount = databaseConnector.getLiveFilesNames().size();
            monitorService.setLastMetricValue(this, liveFilesAmount);
            monitorService.setSnapshotTime(this, String.valueOf(Instant.now().toEpochMilli()));
        }

        @Override
        public void calculateHealthMetric() {
            HealthMetricData healthMetricData = monitorService.getHealthMetricData(this);
            healthMetricData.setLastConditionValue(healthMetricData.getLastMetricValue());
            HealthMetric.calculateHealthValueMetricState(healthMetricData, this);
        }
    },
    BACKUP_HOURLY(BACKUP_HOURLY_LABEL, false, MetricType.BACKUP_METRIC, 2400, 4800, false) {
        public void doSnapshot() {
            long latestBackupStartedTime = dbRecoveryService.getBackupStartedTime();
            HealthMetricData healthMetricData = monitorService.getHealthMetricData(this);
            long prevBackupStartedTime = Math.max(0, healthMetricData.getSpecificLastMetricValue(LATEST_BACKUP_STARTED_TIME));
            monitorService.getHealthMetricData(this).setSpecificLastMetricValue(PREVIOUS_BACKUP_STARTED_TIME, prevBackupStartedTime);
            monitorService.getHealthMetricData(this).setSpecificLastMetricValue(LATEST_BACKUP_STARTED_TIME, latestBackupStartedTime);

            if (latestBackupStartedTime > prevBackupStartedTime) {
                monitorService.setLastMetricValue(this, dbRecoveryService.getBackupSuccess());
            } else {
                monitorService.setLastMetricValue(this, 0);
            }
            monitorService.getHealthMetricData(this).setSpecificLastMetricValue(SNAPSHOT_IN_SECONDS, java.time.Instant.now().getEpochSecond());
            monitorService.setSnapshotTime(this, String.valueOf(Instant.now().toEpochMilli()));
        }

        @Override
        public void calculateHealthMetric() {
            HealthMetricData healthMetricData = monitorService.getHealthMetricData(this);
            if (dbRecoveryService.isBackup()) {
                healthMetricData.setLastConditionValue(healthMetricData.getSpecificLastMetricValue(SNAPSHOT_IN_SECONDS) - healthMetricData.getSpecificLastMetricValue(LATEST_BACKUP_STARTED_TIME));
                HealthMetric.calculateHealthValueMetricState(healthMetricData, this);
            } else {
                healthMetricData.setLastHealthState(BaseNodeMonitorService.HealthState.NA);
            }
        }
    },
    BACKUP_EPOCH(BACKUP_EPOCH_LABEL, false, MetricType.BACKUP_METRIC, 3600, 0, false) {
        public void doSnapshot() {
            long latestBackupStartedTime = Math.max(0, dbRecoveryService.getBackupStartedTime());
            monitorService.setLastMetricValue(this, latestBackupStartedTime);
            monitorService.getHealthMetricData(this).setSpecificLastMetricValue(SNAPSHOT_IN_SECONDS, java.time.Instant.now().getEpochSecond());
            monitorService.setSnapshotTime(this, String.valueOf(Instant.now().toEpochMilli()));
        }

        @Override
        public void calculateHealthMetric() {
            HealthMetricData healthMetricData = monitorService.getHealthMetricData(this);
            if (dbRecoveryService.isBackup()) {
                healthMetricData.setLastConditionValue(healthMetricData.getSpecificLastMetricValue(SNAPSHOT_IN_SECONDS) - healthMetricData.getLastMetricValue());
                HealthMetric.calculateHealthValueMetricState(healthMetricData, this);
            } else {
                healthMetricData.setLastHealthState(BaseNodeMonitorService.HealthState.NA);
            }
        }
    },
    BACKUP_NUMBER_OF_FILES(BACKUP_NUMBER_OF_FILES_LABEL, false, MetricType.BACKUP_METRIC, 1, 1, false) {
        public void doSnapshot() {
            long latestBackupStartedTime = Math.max(0, dbRecoveryService.getBackupStartedTime());
            int backupFilesAmount = dbRecoveryService.getLastBackupInfo() != null ? dbRecoveryService.getLastBackupInfo().numberFiles() : 0;
            if (latestBackupStartedTime > 0) {
                monitorService.setLastMetricValue(this, backupFilesAmount);
                monitorService.getHealthMetricData(this).setSpecificLastMetricValue(LIVE_FILES_AMOUNT, databaseConnector.getLiveFilesNames().size());
            }
            monitorService.setSnapshotTime(this, String.valueOf(Instant.now().toEpochMilli()));
        }

        @Override
        public void calculateHealthMetric() {
            HealthMetricData healthMetricData = monitorService.getHealthMetricData(this);
            if (dbRecoveryService.isBackup()) {
                healthMetricData.setLastConditionValue(healthMetricData.getSpecificLastMetricValue(LIVE_FILES_AMOUNT) - healthMetricData.getLastMetricValue());
                HealthMetric.calculateHealthValueMetricState(healthMetricData, this);
            } else {
                healthMetricData.setLastHealthState(BaseNodeMonitorService.HealthState.NA);
            }
        }
    },
    BACKUP_SIZE(BACKUP_SIZE_LABEL, false, MetricType.BACKUP_METRIC, 0, 0, false) {
        public void doSnapshot() {
            long latestBackupStartedTime = Math.max(0, dbRecoveryService.getBackupStartedTime());
            long backupSize = dbRecoveryService.getLastBackupInfo() != null ? dbRecoveryService.getLastBackupInfo().size() : 0;
            if (latestBackupStartedTime > 0) {
                monitorService.setLastMetricValue(this, backupSize);
            }
            monitorService.setSnapshotTime(this, String.valueOf(Instant.now().toEpochMilli()));
        }

        @Override
        public void calculateHealthMetric() {
            HealthMetricData healthMetricData = monitorService.getHealthMetricData(this);
            if (dbRecoveryService.isBackup()) {
                healthMetricData.setLastConditionValue(-healthMetricData.getLastMetricValue());
                HealthMetric.calculateHealthValueMetricState(healthMetricData, this);
            } else {
                healthMetricData.setLastHealthState(BaseNodeMonitorService.HealthState.NA);
            }
        }
    },
    BACKUP_ENTIRE_DURATION(BACKUP_ENTIRE_DURATION_LABEL, false, MetricType.BACKUP_METRIC, 75, 180, false) {
        public void doSnapshot() {
            monitorService.setLastMetricValue(this, dbRecoveryService.getEntireDuration());
            monitorService.setSnapshotTime(this, String.valueOf(Instant.now().toEpochMilli()));
        }

        @Override
        public void calculateHealthMetric() {
            HealthMetricData healthMetricData = monitorService.getHealthMetricData(this);
            if (dbRecoveryService.isBackup()) {
                healthMetricData.setLastConditionValue(healthMetricData.getLastMetricValue());
                HealthMetric.calculateHealthValueMetricState(healthMetricData, this);
            } else {
                healthMetricData.setLastHealthState(BaseNodeMonitorService.HealthState.NA);
            }
        }
    },
    BACKUP_DURATION(BACKUP_DURATION_LABEL, false, MetricType.BACKUP_METRIC, 45, 90, false) {
        public void doSnapshot() {
            monitorService.setLastMetricValue(this, dbRecoveryService.getBackupDuration());
            monitorService.setSnapshotTime(this, String.valueOf(Instant.now().toEpochMilli()));
        }

        @Override
        public void calculateHealthMetric() {
            HealthMetricData healthMetricData = monitorService.getHealthMetricData(this);
            if (dbRecoveryService.isBackup()) {
                healthMetricData.setLastConditionValue(healthMetricData.getLastMetricValue());
                HealthMetric.calculateHealthValueMetricState(healthMetricData, this);
            } else {
                healthMetricData.setLastHealthState(BaseNodeMonitorService.HealthState.NA);
            }
        }
    },
    BACKUP_UPLOAD_DURATION(BACKUP_UPLOAD_DURATION_LABEL, false, MetricType.BACKUP_METRIC, 20, 60, false) {
        public void doSnapshot() {
            monitorService.setLastMetricValue(this, dbRecoveryService.getUploadDuration());
            monitorService.setSnapshotTime(this, String.valueOf(Instant.now().toEpochMilli()));
        }

        @Override
        public void calculateHealthMetric() {
            HealthMetricData healthMetricData = monitorService.getHealthMetricData(this);
            if (dbRecoveryService.isBackup()) {
                healthMetricData.setLastConditionValue(healthMetricData.getLastMetricValue());
                HealthMetric.calculateHealthValueMetricState(healthMetricData, this);
            } else {
                healthMetricData.setLastHealthState(BaseNodeMonitorService.HealthState.NA);
            }
        }
    },
    BACKUP_REMOVAL_DURATION(BACKUP_REMOVAL_DURATION_LABEL, false, MetricType.BACKUP_METRIC, 10, 30, false) {
        public void doSnapshot() {
            monitorService.setLastMetricValue(this, dbRecoveryService.getRemovalDuration());
            monitorService.setSnapshotTime(this, String.valueOf(Instant.now().toEpochMilli()));
        }

        @Override
        public void calculateHealthMetric() {
            HealthMetricData healthMetricData = monitorService.getHealthMetricData(this);
            if (dbRecoveryService.isBackup()) {
                healthMetricData.setLastConditionValue(healthMetricData.getLastMetricValue());
                HealthMetric.calculateHealthValueMetricState(healthMetricData, this);
            } else {
                healthMetricData.setLastHealthState(BaseNodeMonitorService.HealthState.NA);
            }
        }
    };

    protected static final String SNAPSHOT_TOTAL_TRANSACTIONS_FROM_RECOVERY = "SnapshotTotalTransactionsFromRecovery";
    protected static final String SNAPSHOT_TOTAL_TRANSACTIONS_FROM_LOCAL = "SnapshotTotalTransactionsFromLocal";
    protected static final String SNAPSHOT_PREVIOUS_DSP_CONFIRMED = "SnapshotPreviousDSPConfirmed";
    protected static final String SNAPSHOT_DSP_CONFIRMED = "SnapshotDSPConfirmed";
    protected static final String SNAPSHOT_PREVIOUS_TOTAL_CONFIRMED = "SnapshotPreviousTotalConfirmed";
    protected static final String SNAPSHOT_TOTAL_CONFIRMED = "SnapshotTotalConfirmed";
    protected static final String SNAPSHOT_TCC_CONFIRMED = "SnapshotTccConfirmed";
    protected static final String SNAPSHOT_CONFIRMED = "SnapshotConfirmed";
    protected static final String SNAPSHOT_IN_SECONDS = "SnapshotInSeconds";
    protected static final String PREVIOUS_BACKUP_STARTED_TIME = "previousBackupStartedTime";
    protected static final String LATEST_BACKUP_STARTED_TIME = "latestBackupStartedTime";
    protected static final String LIVE_FILES_AMOUNT = "liveFilesAmount";

    public final String label;
    private final boolean counterBased;
    private final boolean detailedLogs;
    private final MetricType metricType;
    protected ITransactionHelper transactionHelper;
    protected IMonitorService monitorService;
    protected IClusterService clusterService;
    protected TransactionIndexService transactionIndexService;
    protected IConfirmationService confirmationService;
    protected TrustChainConfirmationService trustChainConfirmationService;
    protected ITransactionService transactionService;
    protected IPropagationSubscriber propagationSubscriber;
    protected IWebSocketMessageService webSocketMessageService;
    protected INetworkService networkService;
    protected IReceiver receiver;
    protected IPropagationPublisher propagationPublisher;
    protected IDatabaseConnector databaseConnector;
    protected IDBRecoveryService dbRecoveryService;
    private long warningThreshold;
    private long criticalThreshold;

    HealthMetric(String label, boolean counterBased, MetricType metricType, long warningThreshold, long criticalThreshold, boolean detailedLogs) {
        this.label = label;
        this.counterBased = counterBased;
        this.metricType = metricType;
        this.warningThreshold = warningThreshold;
        this.criticalThreshold = criticalThreshold;
        this.detailedLogs = detailedLogs;
    }

    public static HealthMetric getHealthMetric(String label) {
        return Arrays.stream(HealthMetric.values()).filter(metric -> label.equalsIgnoreCase(metric.label))
                .findFirst().orElseThrow(() -> new IllegalArgumentException("No metric found"));
    }

    private static void calculateHealthCounterMetricState(HealthMetricData healthMetricData, HealthMetric healthMetric, boolean updateCounter) {
        if (healthMetricData.getLastConditionValue() > 0) {
            if (updateCounter) {
                healthMetricData.setLastCounter(healthMetricData.getLastCounter() + 1);
            }
            if (healthMetricData.getLastCounter() >= healthMetric.criticalThreshold &&
                    healthMetric.criticalThreshold >= healthMetric.warningThreshold) {
                healthMetricData.setLastHealthState(BaseNodeMonitorService.HealthState.CRITICAL);
            } else if (healthMetricData.getLastCounter() >= healthMetric.warningThreshold) {
                healthMetricData.setLastHealthState(BaseNodeMonitorService.HealthState.WARNING);
            } else if (BaseNodeMonitorService.HealthState.NA.equals(healthMetricData.getLastHealthState())) {
                healthMetricData.setLastHealthState(BaseNodeMonitorService.HealthState.NORMAL);
            }
        } else {
            if (updateCounter) {
                healthMetricData.setLastCounter(0);
            }
            healthMetricData.setLastHealthState(BaseNodeMonitorService.HealthState.NORMAL);
        }
    }

    private static void calculateHealthValueMetricState(HealthMetricData healthMetricData, HealthMetric healthMetric) {
        long lastConditionValue = healthMetricData.getLastConditionValue();
        if (lastConditionValue >= healthMetric.criticalThreshold &&
                healthMetric.criticalThreshold >= healthMetric.warningThreshold) {
            healthMetricData.setLastHealthState(BaseNodeMonitorService.HealthState.CRITICAL);
        } else if (lastConditionValue >= healthMetric.warningThreshold) {
            healthMetricData.setLastHealthState(BaseNodeMonitorService.HealthState.WARNING);
        } else {
            healthMetricData.setLastHealthState(BaseNodeMonitorService.HealthState.NORMAL);
        }
    }

    public MetricType getMetricType() {
        return metricType;
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
    public Map<HealthMetric, HealthMetricData> getHealthMetrics() {
        return monitorService.getHealthMetrics();
    }

    @Override
    public Health getHealthBuilder(Health.Builder builder) {
        HealthMetricData healthMetricData = getHealthMetricData();
        if (healthMetricData.getLastHealthState().ordinal() == BaseNodeMonitorService.HealthState.CRITICAL.ordinal()) {
            builder.down();
        } else {
            builder.up();
        }
        builder.withDetail(this.label, healthMetricData.getLastHealthState()).withDetail("State", healthMetricData.getLastHealthState())
                .withDetail("conditionValue", healthMetricData.getLastConditionValue());
        if (this.isCounterBased()) {
            builder.withDetail("Counter", healthMetricData.getLastCounter());
        }
        healthMetricData.getAdditionalValues().forEach((key, value) -> builder.withDetail(key, value.toString()));
        return builder.build();
    }

}
