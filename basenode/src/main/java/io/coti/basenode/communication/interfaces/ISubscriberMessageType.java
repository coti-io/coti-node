package io.coti.basenode.communication.interfaces;

import io.coti.basenode.data.NodeType;
import io.coti.basenode.data.interfaces.IPropagatable;

import java.util.function.Consumer;

public interface ISubscriberMessageType {

    Consumer<IPropagatable> getHandler(NodeType publisherNodeType);
}
