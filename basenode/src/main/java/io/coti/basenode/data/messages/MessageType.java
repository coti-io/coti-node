package io.coti.basenode.data.messages;

import io.coti.basenode.data.messages.interfaces.IMessageType;

public enum MessageType {
    STATE(StateMessageData.class, StateMessageType.class),
    VOTE(VoteMessageData.class, VoteMessageType.class);

    private final Class<? extends MessageData> messageClass;
    private final Class<? extends IMessageType> messageTypeClass;

    <S extends MessageData, T extends IMessageType> MessageType(Class<S> messageClass, Class<T> messageTypeClass) {
        this.messageClass = messageClass;
        this.messageTypeClass = messageTypeClass;
    }

    public IMessageType getType(MessageData data) {
        MessageType messageType = getMessageType(data);
        for (IMessageType iMessageType : messageType.messageTypeClass.getEnumConstants()) {
            if (iMessageType.getMessageClass().equals(data.getClass())) {
                return iMessageType;
            }
        }
        throw new IllegalArgumentException("Invalid message type");
    }

    public MessageType getMessageType(MessageData data) {
        for (MessageType messageType : values()) {
            if (messageType.messageClass.isInstance(data)) {
                return messageType;
            }
        }
        throw new IllegalArgumentException("Invalid message type");
    }
}
