package io.coti.basenode.communication.interfaces;

import io.coti.basenode.data.NodeType;

import java.util.HashMap;
import java.util.function.Consumer;

public interface IPropagationSubscriber {

    void startListening();

    void addMessageHandler(HashMap<String, Consumer<Object>> messagesHandler);

    void connectAndSubscribeToServer(String propagationServerAddressAndPort);

    void initPropagationHandler();

    void subscribeAll(String serverAddress);

    void disconnect(String propagationFullAddress, NodeType nodeType);

    void shutdown();
}