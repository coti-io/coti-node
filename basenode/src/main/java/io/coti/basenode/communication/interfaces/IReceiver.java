package io.coti.basenode.communication.interfaces;

import java.util.HashMap;
import java.util.function.Consumer;

public interface IReceiver {
    void init(String receivingPort, HashMap<String, Consumer<Object>> classNameToHandlerMapping);

    void startListening();

    void initReceiverHandler();

    void shutdown();
}
