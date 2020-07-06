package io.coti.basenode.data.messages;

import io.coti.basenode.data.messages.interfaces.IMessageType;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public enum StateMessageType implements IMessageType {
    CLUSTER_STAMP_INITIATED(InitiateClusterStampStateMessageData.class),
    CLUSTER_STAMP_PREPARE_INDEX(LastIndexClusterStampStateMessageData.class),
    CLUSTER_STAMP_PREPARE_HASH(HashClusterStampStateMessageData.class),
    CLUSTER_STAMP_CONTINUE(ContinueClusterStampStateMessageData.class),
    CLUSTER_STAMP_EXECUTE(ExecuteClusterStampStateMessageData.class);

    private final Class<? extends StateMessageData> messageClass;

    private static class StateMessageTypes {
        private static final Map<Class<? extends StateMessageData>, StateMessageType> messageClassToTypeMap = new HashMap<>();
    }

    <T extends StateMessageData> StateMessageType(Class<T> messageClass) {
        this.messageClass = messageClass;
        StateMessageTypes.messageClassToTypeMap.put(messageClass, this);
    }

    @Override
    public Class<? extends MessageData> getMessageClass() {
        return messageClass;
    }

    public static StateMessageType getName(Class<?> messageClass) {
        return Optional.ofNullable(StateMessageTypes.messageClassToTypeMap.get(messageClass))
                .orElseThrow(() -> new IllegalArgumentException("Invalid state message type"));
    }

}
