package io.coti.basenode.communication.interfaces;

import io.coti.basenode.communication.ZeroMQSubscriberQueue;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.data.interfaces.IPropagatable;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public interface IPropagationSubscriber {

    void init();

    void setSubscriberNodeType(NodeType subscriberNodeType);

    void setPublisherNodeTypeToMessageTypesMap(EnumMap<NodeType, List<Class<? extends IPropagatable>>> publisherNodeTypeToMessageTypesMap);

    void initMonitor();

    void connectAndSubscribeToServer(String publisherAddressAndPort, NodeType publisherNodeType);

    void initPropagationHandler();

    void disconnect(String propagationFullAddress, NodeType nodeType);

    int getMessageQueueSize(ZeroMQSubscriberQueue zeroMQSubscriberQueue);

    Map<String, String> getQueueSizeMap();

    void shutdown();
}
