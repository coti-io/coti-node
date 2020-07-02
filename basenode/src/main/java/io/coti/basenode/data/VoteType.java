package io.coti.basenode.data;

public enum VoteType implements IMessageType{
    CLUSTER_STAMP(ClusterStampVoteData.class);

    private Class<? extends VoteMessage> messageClass;

    <T extends VoteMessage> VoteType(Class<T> messageClass) {
        this.messageClass = messageClass;
    }

    @Override
    public Class<? extends Message> getMessageClass() {
        return messageClass;
    }
}
