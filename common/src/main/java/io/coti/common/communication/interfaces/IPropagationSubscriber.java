package io.coti.common.communication.interfaces;

import java.util.Map;
import java.util.function.Consumer;

public interface IPropagationSubscriber {

    void init(Map<String, Consumer<Object>> messagesHandler);
}