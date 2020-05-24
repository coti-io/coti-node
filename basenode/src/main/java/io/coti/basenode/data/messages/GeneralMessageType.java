package io.coti.basenode.data.messages;

public enum GeneralMessageType {
    CLUSTER_STAMP_INITIATED(StateMessageClusterStampInitiatedPayload.class),
    CLUSTER_STAMP_PREPARE_INDEX(StateMessageLastClusterStampIndexPayload.class),
    CLUSTER_STAMP_INDEX_VOTE(GeneralVoteClusterStampIndexPayload.class),
    CLUSTER_STAMP_PREPARE_HASH(null),
    CLUSTER_STAMP_HASH_VOTE(null),
    CLUSTER_STAMP_EXECUTE(StateMessageClusterStampExecutePayload.class);

    private Class<? extends MessagePayload> messagePayload;

    private <T extends MessagePayload> GeneralMessageType(Class<? extends MessagePayload> messagePayload) {
        this.messagePayload = messagePayload;
    }

    public Class<? extends MessagePayload> getMessagePayload() {
        return messagePayload;
    }

    public static GeneralMessageType getGeneralMessageType(Class<?> messagePayload) {
        for (GeneralMessageType generalMessageType : values()) {
            if (generalMessageType.getMessagePayload() == messagePayload) {
                return generalMessageType;
            }
        }
        return null;
    }
}
