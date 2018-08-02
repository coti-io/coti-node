package io.coti.common.communication.interfaces;

import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

public interface IPropagationSubscriber {

    void init(HashMap<String, Consumer<Object>> messagesHandler);
}