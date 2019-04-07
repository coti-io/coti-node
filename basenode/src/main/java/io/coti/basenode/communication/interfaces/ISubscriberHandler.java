package io.coti.basenode.communication.interfaces;

import io.coti.basenode.data.NodeType;

import java.util.function.Consumer;
import java.util.function.Function;

public interface ISubscriberHandler {

    void init();

    Function<NodeType, Consumer<Object>> get(String messageType);

}
