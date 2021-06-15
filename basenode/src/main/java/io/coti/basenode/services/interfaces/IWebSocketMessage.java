package io.coti.basenode.services.interfaces;

public interface IWebSocketMessage {
    void convertAndSend(String s, Object obj);

    int getMessageQueueSize();
}
