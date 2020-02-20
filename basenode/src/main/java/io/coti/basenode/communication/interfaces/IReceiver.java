package io.coti.basenode.communication.interfaces;

import java.util.HashMap;
import java.util.function.Consumer;

public interface IReceiver {
    Thread init(String receivingPort, HashMap<String, Consumer<Object>> classNameToHandlerMapping);
}
