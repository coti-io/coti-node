package io.coti.basenode.communication.interfaces;

import io.coti.basenode.data.NodeType;
import io.coti.basenode.data.interfaces.IPropagatable;

import java.util.List;

public interface IPropagationPublisher {

    void init(String propagationPort, NodeType publisherNodeType);

    <T extends IPropagatable> void propagate(T toPropagate, List<NodeType> subscriberNodeTypes);

    void shutdown();
}
