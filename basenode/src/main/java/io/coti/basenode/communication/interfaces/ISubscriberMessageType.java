package io.coti.basenode.communication.interfaces;

import io.coti.basenode.data.NodeType;

import java.util.function.Consumer;

public interface ISubscriberMessageType {
    Consumer<Object> getHandler(NodeType publisherNodeType);
}
