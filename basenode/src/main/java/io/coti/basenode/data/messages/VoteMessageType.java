package io.coti.basenode.data.messages;

public enum VoteMessageType implements IMessageType {
    CLUSTER_STAMP_INDEX_VOTE(LastIndexClusterStampVoteMessageData.class),
    CLUSTER_STAMP_HASH_VOTE(HashClusterStampVoteMessageData.class),
    CLUSTER_STAMP_AGREED_HASH_HISTORY_NODE(AgreedHashClusterStampVoteMessageData.class);

    private Class<? extends VoteMessageData> messageClass;

    <T extends VoteMessageData> VoteMessageType(Class<T> messageClass) {
        this.messageClass = messageClass;
    }

    @Override
    public Class<? extends MessageData> getMessageClass() {
        return messageClass;
    }

    public static VoteMessageType getName(Class<?> messageClass) {
        for (VoteMessageType name : values()) {
            if (name.getMessageClass() == messageClass) {
                return name;
            }
        }
        return null;
    }

}
