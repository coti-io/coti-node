package io.coti.basenode.data.messages;

public interface IMessageType {

    Class<? extends MessageData> getMessageClass();

}

