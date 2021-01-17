package io.coti.basenode.services;

import io.coti.basenode.communication.interfaces.IPropagationPublisher;
import io.coti.basenode.communication.interfaces.IPropagationSubscriber;
import io.coti.basenode.communication.interfaces.IReceiver;
import io.coti.basenode.communication.interfaces.ISender;
import io.coti.basenode.data.AddressData;
import io.coti.basenode.data.NetworkData;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.data.interfaces.IPropagatable;
import io.coti.basenode.services.interfaces.ICommunicationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Consumer;

@Slf4j
@Service
public class CommunicationService implements ICommunicationService {

    @Autowired
    protected IReceiver receiver;
    @Autowired
    private IPropagationSubscriber propagationSubscriber;
    @Autowired
    private IPropagationPublisher propagationPublisher;
    @Autowired
    private ISender sender;

    @Override
    public void initSubscriber(NodeType subscriberNodeType, EnumMap<NodeType, List<Class<? extends IPropagatable>>> initialPublisherNodeTypeToMessageTypesMap) {
        propagationSubscriber.init();
        propagationSubscriber.setSubscriberNodeType(subscriberNodeType);
        initialPublisherNodeTypeToMessageTypesMap.putIfAbsent(NodeType.NodeManager, Collections.singletonList(NetworkData.class));
        initialPublisherNodeTypeToMessageTypesMap.putIfAbsent(NodeType.DspNode, Arrays.asList(TransactionData.class, AddressData.class));
        propagationSubscriber.setPublisherNodeTypeToMessageTypesMap(initialPublisherNodeTypeToMessageTypesMap);
    }

    @Override
    public void initReceiver(String receivingPort, HashMap<String, Consumer<IPropagatable>> classNameToReceiverHandlerMapping) {
        receiver.init(receivingPort, classNameToReceiverHandlerMapping);
        receiver.startListening();
    }

    @Override
    public void addSender(String receivingServerAddress, NodeType nodeType) {
        sender.connectToNode(receivingServerAddress, nodeType);
    }

    @Override
    public void removeSender(String receivingFullAddress, NodeType nodeType) {
        sender.disconnectFromNode(receivingFullAddress, nodeType);
    }

    @Override
    public void addSubscription(String propagationServerAddress, NodeType publisherNodeType) {
        propagationSubscriber.connectAndSubscribeToServer(propagationServerAddress, publisherNodeType);
    }

    @Override
    public void removeSubscription(String propagationServerAddress, NodeType publisherNodeType) {
        propagationSubscriber.disconnect(propagationServerAddress, publisherNodeType);
    }

    @Override
    public void initPublisher(String propagationPort, NodeType propagatorType) {
        propagationPublisher.init(propagationPort, propagatorType);
    }
}
