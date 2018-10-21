package io.coti.basenode.communication.interfaces;

import java.util.HashMap;
import java.util.function.Consumer;

public interface IPropagationSubscriber {


    void startListeneing();

    void addMessageHandler(HashMap<String, Consumer<Object>> messagesHandler);

    void shutdown();

    void addAddress(String propagationServerAddress, String propagationServerPort);

    void addAddress(String propagationServerAddressAndPort);
}