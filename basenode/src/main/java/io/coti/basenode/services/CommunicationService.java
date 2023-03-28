package io.coti.basenode.services;

import io.coti.basenode.data.*;
import io.coti.basenode.data.interfaces.IPropagatable;
import io.coti.basenode.exceptions.CotiRunTimeException;
import io.coti.basenode.services.interfaces.ICommunicationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Consumer;

import static io.coti.basenode.services.BaseNodeServiceManager.*;

@Slf4j
@Service
public class CommunicationService implements ICommunicationService {

    @Override
    public void initSubscriber(NodeType subscriberNodeType, EnumMap<NodeType, List<Class<? extends IPropagatable>>> initialPublisherNodeTypeToMessageTypesMap) {
        propagationSubscriber.init();
        propagationSubscriber.setSubscriberNodeType(subscriberNodeType);
        initialPublisherNodeTypeToMessageTypesMap.putIfAbsent(NodeType.NodeManager, Collections.singletonList(NetworkData.class));
        initialPublisherNodeTypeToMessageTypesMap.putIfAbsent(NodeType.DspNode, Arrays.asList(TransactionData.class, AddressData.class, RejectedTransactionData.class, TransactionsStateData.class));
        propagationSubscriber.setPublisherNodeTypeToMessageTypesMap(initialPublisherNodeTypeToMessageTypesMap);
    }

    @Override
    public void initReceiver(String receivingPort, HashMap<String, Consumer<IPropagatable>> classNameToReceiverHandlerMapping) {
        zeroMQReceiver.init(receivingPort, classNameToReceiverHandlerMapping);
        zeroMQReceiver.startListening();
    }

    @Override
    public void addSender(String receivingServerAddress, NodeType nodeType) {
        zeroMQSender.connectToNode(receivingServerAddress, nodeType);
    }

    @Override
    public void removeSender(String receivingFullAddress, NodeType nodeType) {
        zeroMQSender.disconnectFromNode(receivingFullAddress, nodeType);
    }

    @Override
    public void senderReconnect(String receivingFullAddress, NodeType nodeType) {
        if (zeroMQSender.isNewlyConnectedToNode(receivingFullAddress)) {
            return;
        }
        int numberOfReconnectAttempts = 0;
        do {
            if (zeroMQSender.isConnectedToNode(receivingFullAddress)) {
                zeroMQSender.disconnectFromNode(receivingFullAddress, nodeType);
            }
            zeroMQSender.connectToNode(receivingFullAddress, nodeType);
            waitUntilConnected(receivingFullAddress);
            numberOfReconnectAttempts++;
        } while (numberOfReconnectAttempts < 3 && !zeroMQSender.isConnectedToNode(receivingFullAddress));
    }

    private static void waitUntilConnected(String receivingFullAddress) {
        int numberOfChecks = 0;
        while (numberOfChecks < 25 && !zeroMQSender.isConnectedToNode(receivingFullAddress)) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new CotiRunTimeException(String.format("Error while sleep waiting for sender reconnect, error: %s", e));
            }
            numberOfChecks++;
        }
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
