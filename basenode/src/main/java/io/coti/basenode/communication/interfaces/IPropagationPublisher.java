package io.coti.basenode.communication.interfaces;

import io.coti.basenode.data.NodeType;
import io.coti.basenode.data.interfaces.IEntity;

import java.util.List;

public interface IPropagationPublisher {

    void init(String propagationPort);

    <T extends IEntity> void propagate(T toPropagate, List<NodeType> subscriberNodeTypes);

    void shutdown();
}
