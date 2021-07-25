package io.coti.basenode.communication.interfaces;

import io.coti.basenode.data.interfaces.IPropagatable;

import java.util.HashMap;
import java.util.function.Consumer;

public interface IReceiver {

    void init(String receivingPort, HashMap<String, Consumer<IPropagatable>> classNameToHandlerMapping);

    void initMonitor();

    void startListening();

    void initReceiverHandler();

    void shutdown();
}
