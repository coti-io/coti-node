package io.coti.basenode.services.interfaces;

public interface IWebSocketMessageService {
    void convertAndSend(String s, Object obj);

    int getMessageQueueSize();
}
