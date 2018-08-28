package io.coti.common.communication.interfaces;

import io.coti.common.NodeType;
import io.coti.common.data.interfaces.IEntity;

import java.util.List;

public interface IPropagationPublisher {

    void init(String propagationPort);

    <T extends IEntity> void propagate(T toPropagate, List<NodeType> subscriberNodeTypes);
}
