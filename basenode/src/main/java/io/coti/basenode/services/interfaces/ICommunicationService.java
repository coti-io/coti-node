package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.NodeType;
import io.coti.basenode.data.interfaces.IPropagatable;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

public interface ICommunicationService {
    void initSubscriber(NodeType nodeType, EnumMap<NodeType, List<Class<? extends IPropagatable>>> publisherNodeTypeToMessageTypesMap);

    void initReceiver(String receivingPort, HashMap<String, Consumer<Object>> classNameToReceiverHandlerMapping);

    void addSender(String receivingServerAddress);

    void removeSender(String receivingFullAddress, NodeType nodeType);

    void addSubscription(String propagationServerAddress, NodeType publisherNodeType);

    void removeSubscription(String propagationServerAddress, NodeType publisherNodeType);

    void initPublisher(String propagationPort, NodeType propagatorType);
}
