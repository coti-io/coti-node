package io.coti.basenode.data;

public enum MessageType {
    STATE(StateMessage.class, StateType.class),
    VOTE(VoteMessage.class, VoteType.class);

    private Class<? extends Message> messageClass;
    private Class<? extends IMessageType> messageTypeClass;

    <S extends Message, T extends IMessageType> MessageType(Class<S> messageClass, Class<T> messageTypeClass) {
        this.messageClass = messageClass;
        this.messageTypeClass = messageTypeClass;
    }

    public IMessageType getType(Message data) {
        for (MessageType messageType : values()) {
            if (messageType.messageClass.isInstance(data)) {
                for (IMessageType iMessageType : messageType.messageTypeClass.getEnumConstants()) {
                    if (iMessageType.getMessageClass().equals(data.getClass())) {
                        return iMessageType;
                    }
                }


            }
        }
    }

}
