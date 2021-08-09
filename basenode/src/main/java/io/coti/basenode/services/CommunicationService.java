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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
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

    private AtomicInteger historicInvalidSenders = new AtomicInteger(0);

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

    @Override
    public int resetHistoricInvalidSendersSize() {
        return historicInvalidSenders.getAndSet(0);
    }


    @Scheduled(initialDelay = 1000, fixedDelay = 5000)
    private void validateSenders() {
        Map<String, NodeType> invalidSenders = null;
        try {
            invalidSenders = sender.validateSenders();
        } catch (IOException e) {
            log.error("Exception in isPortOpened: " + e);
        }
        if (invalidSenders != null && invalidSenders.size() > 0) {
            for (Map.Entry<String, NodeType> entry : invalidSenders.entrySet()) {
                String receivingFullAddress = entry.getKey();
                NodeType nodeType = entry.getValue();
                log.error("invalid Sender: " + receivingFullAddress + " , removing and adding it.");
                removeSender(receivingFullAddress, nodeType);
                addSender(receivingFullAddress, nodeType);
            }
            historicInvalidSenders.compareAndSet(BaseNodeMetricsService.MAX_NUMBER_OF_NON_FETCHED_SAMPLES,0);
            historicInvalidSenders.addAndGet(invalidSenders.size());
            invalidSenders.clear();
        }
    }
}
