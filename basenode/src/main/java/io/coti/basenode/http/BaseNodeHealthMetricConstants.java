package io.coti.basenode.http;

public class BaseNodeHealthMetricConstants {

    public static final String TOTAL_TRANSACTIONS_LABEL = "TotalTransactions";
    public static final String SOURCES_UPPER_BOUND_LABEL = "SourcesUpperBound";
    public static final String SOURCES_LOWER_BOUND_LABEL = "SourcesLowerBound";
    public static final String INDEX_LABEL = "Index";
    public static final String WAITING_DSP_CONSENSUS_RESULTS_CONFIRMED_LABEL = "WaitingDspConsensusResultsConfirmed";

    public static final String DSP_CONFIRMED_LABEL = "DSPConfirmed";
    public static final String TOTAL_CONFIRMED_LABEL = "TotalConfirmed";
    public static final String TRUST_CHAIN_CONFIRMED_LABEL = "TrustChainConfirmed";
    public static final String WAITING_MISSING_TRANSACTION_INDEXES_LABEL = "WaitingMissingTransactionIndexes";
    public static final String TOTAL_POSTPONED_TRANSACTIONS_LABEL = "TotalPostponedTransactions";
    public static final String PROPAGATION_QUEUE_LABEL = "PropagationQueue";
    public static final String REJECTED_TRANSACTIONS_LABEL = "RejectedTransactions";

    public static final String WEB_SOCKET_MESSAGES_QUEUE_LENGTH_LABEL = "WebSocketMessagesQueueLength";
    public static final String CONFIRMATION_QUEUE_SIZE_LABEL = "ConfirmationQueueSize";
    public static final String PERCENTAGE_USED_HEAP_MEMORY_LABEL = "PercentageUsedHeapMemory";
    public static final String PERCENTAGE_USED_MEMORY_LABEL = "PercentageUsedMemory";
    public static final String CONNECTED_TO_RECOVERY_LABEL = "ConnectedToRecovery";
    public static final String PROPAGATION_SUBSCRIBER_TRANSACTIONS_STATE_QUEUE_SIZE_LABEL = "PropagationSubscriberTransactionsStateQueueSize";

    public static final String PROPAGATION_SUBSCRIBER_NETWORK_QUEUE_SIZE_LABEL = "PropagationSubscriberNetworkQueueSize";
    public static final String PROPAGATION_SUBSCRIBER_ADDRESS_QUEUE_SIZE_LABEL = "PropagationSubscriberAddressQueueSize";
    public static final String PROPAGATION_SUBSCRIBER_TRANSACTION_QUEUE_SIZE_LABEL = "PropagationSubscriberTransactionQueueSize";
    public static final String PROPAGATION_SUBSCRIBER_HEARTBEAT_QUEUE_SIZE_LABEL = "PropagationSubscriberHeartbeatQueueSize";
    public static final String ZERO_MQ_RECEIVER_QUEUE_SIZE_LABEL = "ZeroMQReceiverQueueSize";
    public static final String PROPAGATION_PUBLISHER_QUEUE_SIZE_LABEL = "PropagationPublisherQueueSize";

    public static final String LIVE_FILES_SIZE_LABEL = "LiveFilesSize";

    public static final String BACKUP_HOURLY_LABEL = "BackupHourly";
    public static final String BACKUP_EPOCH_LABEL = "BackupEpoch";
    public static final String BACKUP_NUMBER_OF_FILES_LABEL = "BackupNumberOfFiles";
    public static final String BACKUP_SIZE_LABEL = "BackupSize";
    public static final String BACKUP_ENTIRE_DURATION_LABEL = "BackupEntireDuration";
    public static final String BACKUP_DURATION_LABEL = "BackupDuration";
    public static final String BACKUP_UPLOAD_DURATION_LABEL = "BackupUploadDuration";
    public static final String BACKUP_REMOVAL_DURATION_LABEL = "BackupRemovalDuration";


    protected BaseNodeHealthMetricConstants() {
    }
}
