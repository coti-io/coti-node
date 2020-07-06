package io.coti.basenode.data.messages;

public enum StateMessageType implements IMessageType{
    CLUSTER_STAMP_INITIATED(InitiateClusterStampStateMessageData.class),
    CLUSTER_STAMP_PREPARE_INDEX(LastIndexClusterStampStateMessageData.class),
    CLUSTER_STAMP_PREPARE_HASH(HashClusterStampStateMessageData.class),
    CLUSTER_STAMP_CONTINUE(ContinueClusterStampStateMessageData.class),
    CLUSTER_STAMP_EXECUTE(ExecuteClusterStampStateMessageData.class);

    private Class<? extends StateMessageData> messageClass;

    <T extends StateMessageData> StateMessageType(Class<T> messageClass) {
        this.messageClass = messageClass;
    }

    @Override
    public Class<? extends MessageData> getMessageClass() {
        return messageClass;
    }

    public static StateMessageType getName(Class<?> messageClass) {
        for (StateMessageType name : values()) {
            if (name.getMessageClass() == messageClass) {
                return name;
            }
        }
        return null;
    }

}
