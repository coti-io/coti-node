package io.coti.basenode.data;

public enum StateType implements IMessageType{
    CLUSTER_STAMP(ClusterStampStateData.class);

    private Class<? extends StateMessage> messageClass;

    <T extends StateMessage> StateType(Class<T> messageClass) {
        this.messageClass = messageClass;
    }

    @Override
    public Class<? extends Message> getMessageClass() {
        return messageClass;
    }
}
