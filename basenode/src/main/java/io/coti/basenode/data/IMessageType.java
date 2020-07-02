package io.coti.basenode.data;

public interface IMessageType {

    Class<? extends Message> getMessageClass();
}
