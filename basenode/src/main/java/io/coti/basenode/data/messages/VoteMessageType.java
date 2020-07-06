package io.coti.basenode.data.messages;

import io.coti.basenode.data.messages.interfaces.IMessageType;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public enum VoteMessageType implements IMessageType {
    CLUSTER_STAMP_INDEX_VOTE(LastIndexClusterStampVoteMessageData.class),
    CLUSTER_STAMP_HASH_VOTE(HashClusterStampVoteMessageData.class),
    CLUSTER_STAMP_AGREED_HASH_HISTORY_NODE(AgreedHashClusterStampVoteMessageData.class);

    private final Class<? extends VoteMessageData> messageClass;

    private static class VoteMessageTypes {
        private static final Map<Class<? extends VoteMessageData>, VoteMessageType> messageClassToTypeMap = new HashMap<>();
    }

    <T extends VoteMessageData> VoteMessageType(Class<T> messageClass) {
        this.messageClass = messageClass;
        VoteMessageTypes.messageClassToTypeMap.put(messageClass, this);
    }

    @Override
    public Class<? extends MessageData> getMessageClass() {
        return messageClass;
    }

    public static VoteMessageType getName(Class<?> messageClass) {
        return Optional.ofNullable(VoteMessageTypes.messageClassToTypeMap.get(messageClass))
                .orElseThrow(() -> new IllegalArgumentException("Invalid vote message type"));
    }

}
