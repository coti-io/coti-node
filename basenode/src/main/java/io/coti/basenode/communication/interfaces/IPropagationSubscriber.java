package io.coti.basenode.communication.interfaces;

import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

public interface IPropagationSubscriber {

    void init(HashMap<String, Consumer<Object>> messagesHandler);

    void addAddress(String propagationServerAddress, String propagationServerPort);

    void addAddress(String propagationServerAddressAndPort);

    void subscribeToChannels();
}