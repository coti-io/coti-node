package io.coti.basenode.constants;

public class BaseNodeHealthMetricConstants {

    public static final String TOTAL_TRANSACTIONS_DELTA_LABEL = "TotalTransactionsDelta";
    public static final String TOTAL_TRANSACTIONS_LABEL = "Transactions";
    public static final String TOTAL_TRANSACTIONS_FROM_RECOVERY_LABEL = "TotalNumberOfTransactionsFromRecovery";
    public static final String SOURCES_LABEL = "Sources";
    public static final String SOURCES_UPPER_BOUND_LABEL = "SourcesUpperBound";
    public static final String SOURCES_LOWER_BOUND_LABEL = "SourcesLowerBound";
    public static final String INDEX_DELTA_LABEL = "IndexDelta";
    public static final String INDEX_LABEL = "LastIndex";
    public static final String WAITING_DCR_QUEUE_LABEL = "WaitingDspConsensus";
    public static final String DSP_CONFIRMED_LABEL_DELTA = "DSPConfirmedDelta";
    public static final String DSP_CONFIRMED_LABEL = "DspConfirmed";
    public static final String TOTAL_CONFIRMED_LABEL = "Confirmed";
    public static final String TRUST_CHAIN_CONFIRMED_LABEL = "TccConfirmed";
    public static final String NUMBER_OF_TIMES_TCC_NOT_CHANGED_LABEL = "NumberOfTimesTrustScoreNotChanged";
    public static final String WAITING_MISSING_TRANSACTION_INDEXES_LABEL = "WaitingMissingTransactionIndexes";
    public static final String TOTAL_POSTPONED_TRANSACTIONS_LABEL = "PostponedTransactions";
    public static final String RESEND_UNCONFIRMED_RETRIES_COUNTER_LABEL = "ResendUnconfirmedRetriesCounter";
    public static final String REJECTED_TRANSACTIONS_LABEL = "RejectedTransactions";
    public static final String WEB_SOCKET_MESSAGES_QUEUE_LABEL = "WebSocketMessagesQueue";
    public static final String TCC_CONFIRMATION_QUEUE_LABEL = "TccConfirmationQueueSize";
    public static final String DCR_CONFIRMATION_QUEUE_SIZE_LABEL = "DcrConfirmationQueueSize";
    public static final String PERCENTAGE_USED_HEAP_MEMORY_LABEL = "PercentageUsedHeapMemory";
    public static final String PERCENTAGE_USED_MEMORY_LABEL = "PercentageUsedMemory";
    public static final String CONNECTED_TO_RECOVERY_LABEL = "ConnectedToRecovery";
    public static final String PROPAGATION_SUBSCRIBER_TRANSACTIONS_STATE_QUEUE_SIZE_LABEL = "PropagationSubscriberTransactionsStateQueueSize";
    public static final String PROPAGATION_SUBSCRIBER_NETWORK_QUEUE_SIZE_LABEL = "PropagationSubscriberNetworkQueueSize";
    public static final String PROPAGATION_SUBSCRIBER_ADDRESS_QUEUE_SIZE_LABEL = "PropagationSubscriberAddressQueueSize";
    public static final String PROPAGATION_SUBSCRIBER_TRANSACTION_QUEUE_SIZE_LABEL = "PropagationSubscriberTransactionQueueSize";
    public static final String PROPAGATION_SUBSCRIBER_HEARTBEAT_QUEUE_SIZE_LABEL = "PropagationSubscriberHeartbeatQueueSize";
    public static final String ZERO_MQ_RECEIVER_QUEUE_SIZE_LABEL = "ZeroMQReceiverQueueSize";
    public static final String ZERO_MQ_SOCKET_DISCONNECTS_LABEL = "ZeroMQSocketDisconnects";
    public static final String PROPAGATION_PUBLISHER_QUEUE_SIZE_LABEL = "PropagationPublisherQueueSize";
    public static final String LIVE_FILES_SIZE_LABEL = "LiveFilesSize";
    public static final String LAST_BACKUP_ELAPSED_LABEL = "LastBackupElapsedSeconds";
    public static final String BACKUP_STARTED_TIME_LABEL = "BackupStartedTime";
    public static final String NUMBER_OF_LIVE_FILES_NOT_BACKED_UP_LABEL = "NumberOfLivesFilesNotBackedUp";
    public static final String BACKED_UP_NUMBER_OF_FILES_LABEL = "BackupNumberOfFiles";
    public static final String BACKUP_SIZE_LABEL = "BackupSize";
    public static final String BACKUP_ENTIRE_DURATION_LABEL = "BackupEntireDuration";
    public static final String BACKUP_DURATION_LABEL = "BackupDuration";
    public static final String BACKUP_UPLOAD_DURATION_LABEL = "BackupUploadDuration";
    public static final String BACKUP_REMOVAL_DURATION_LABEL = "BackupRemovalDuration";


    private BaseNodeHealthMetricConstants() {
    }
}
