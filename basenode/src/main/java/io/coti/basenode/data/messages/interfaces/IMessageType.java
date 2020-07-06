package io.coti.basenode.data.messages.interfaces;

import io.coti.basenode.data.messages.MessageData;

public interface IMessageType {

    Class<? extends MessageData> getMessageClass();

}

