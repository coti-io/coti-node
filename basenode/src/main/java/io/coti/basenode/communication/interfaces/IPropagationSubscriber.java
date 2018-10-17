package io.coti.basenode.communication.interfaces;

import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

public interface IPropagationSubscriber {

    void init(List<String> propagationServerAddresses, HashMap<String, Consumer<Object>> messagesHandler);

    void shutdown();
}