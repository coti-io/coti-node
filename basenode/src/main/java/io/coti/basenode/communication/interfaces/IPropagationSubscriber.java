package io.coti.basenode.communication.interfaces;

import java.util.HashMap;
import java.util.function.Consumer;

public interface IPropagationSubscriber {


    void startListeneing();

    void addMessageHandler(HashMap<String, Consumer<Object>> messagesHandler);

    void shutdown();

    void connectAndSubscribeToServer(String propagationServerAddressAndPort);

    void initPropagationHandler();

    void subscribeAll(String serverAddress);
}