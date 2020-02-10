package io.coti.basenode.communication;

import io.coti.basenode.communication.data.ConnectedNodeData;
import io.coti.basenode.communication.data.ZeroMQMessageData;
import io.coti.basenode.communication.interfaces.IPropagationSubscriber;
import io.coti.basenode.communication.interfaces.ISerializer;
import io.coti.basenode.communication.interfaces.ISubscriberHandler;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.data.interfaces.IEntity;
import io.coti.basenode.data.interfaces.IPropagatable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import org.zeromq.ZMQException;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.Instant;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
@Service
public class ZeroMQSubscriber implements IPropagationSubscriber {

    private static final int HEARTBEAT_INTERVAL = 10000;
    private static final int INITIAL_DELAY = 5000;
    private static final int FIXED_DELAY = 5000;
    private ZMQ.Context zeroMQContext;
    private ZMQ.Socket propagationReceiver;
    private Map<String, ConnectedNodeData> connectedNodes = new ConcurrentHashMap<>();
    private BlockingQueue<ZeroMQMessageData> messageQueue;
    private Thread propagationReceiverThread;
    private Thread messagesQueueHandlerThread;
    @Autowired
    private ISerializer serializer;
    private EnumMap<NodeType, List<Class<? extends IPropagatable>>> publisherNodeTypeToMessageTypesMap;
    private NodeType subscriberNodeType;
    @Autowired
    private ISubscriberHandler subscriberHandler;

    @PostConstruct
    private void init() {
        initSockets();
        messageQueue = new LinkedBlockingQueue<>();
        subscriberHandler.init();
    }

    public void initSockets() {
        zeroMQContext = ZMQ.context(1);
        propagationReceiver = zeroMQContext.socket(SocketType.SUB);
        propagationReceiver.setHWM(10000);
        ZeroMQUtils.bindToRandomPort(propagationReceiver);
    }

    @Override
    public void setSubscriberNodeType(NodeType subscriberNodeType) {
        this.subscriberNodeType = subscriberNodeType;
    }

    @Override
    public void setPublisherNodeTypeToMessageTypesMap(EnumMap<NodeType, List<Class<? extends IPropagatable>>> publisherNodeTypeToMessageTypesMap) {
        this.publisherNodeTypeToMessageTypesMap = publisherNodeTypeToMessageTypesMap;
    }

    @Override
    public void startListening() {
        propagationReceiverThread = new Thread(() -> {
            boolean contextTerminated = false;
            while (!contextTerminated && !Thread.currentThread().isInterrupted()) {
                try {
                    String channel = propagationReceiver.recvStr();
                    log.debug("Received a new message on channel: {}", channel);
                    byte[] message = propagationReceiver.recv();
                    messageQueue.put(new ZeroMQMessageData(channel, message));
                } catch (InterruptedException e) {
                    log.info("ZMQ subscriber propagation receiver interrupted");
                    Thread.currentThread().interrupt();
                } catch (ZMQException e) {
                    if (e.getErrorCode() == ZMQ.Error.ETERM.getCode()) {
                        contextTerminated = true;
                    } else {
                        log.error("ZeroMQ exception at receiver thread", e);
                    }
                }
            }
            propagationReceiver.close();
        });
        propagationReceiverThread.start();


    }

    public void initPropagationHandler() {
        messagesQueueHandlerThread = new Thread(this::handleMessagesQueueTask);
        messagesQueueHandlerThread.start();
    }

    private void handleMessagesQueueTask() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                ZeroMQMessageData zeroMQMessageData = messageQueue.take();
                log.debug("ZMQ message arrived: {}", zeroMQMessageData);
                propagationProcess(zeroMQMessageData);
            } catch (InterruptedException e) {
                log.info("ZMQ subscriber message handler interrupted");
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                log.error("ZMQ message handler task error", e);
            }
        }
        LinkedList<ZeroMQMessageData> remainingMessages = new LinkedList<>();
        messageQueue.drainTo(remainingMessages);
        if (!remainingMessages.isEmpty()) {
            log.info("Please wait to process {} remaining messages", remainingMessages.size());
            remainingMessages.forEach(zeroMQMessageData -> {
                try {
                    propagationProcess(zeroMQMessageData);
                } catch (Exception e) {
                    log.error("ZMQ message handler task error", e);
                }
            });
        }

    }

    private void propagationProcess(ZeroMQMessageData zeroMQMessageData) throws ClassNotFoundException {
        String channel = zeroMQMessageData.getChannel();
        byte[] message = zeroMQMessageData.getMessage();
        if (channel.contains("HeartBeat")) {
            String serverAddress = new String(message);
            updatePublisherLastConnectionTime(serverAddress);
        } else {
            String[] channelArray = channel.split("-");
            Class<? extends IPropagatable> propagatedMessageType = (Class<? extends IPropagatable>) Class.forName(channelArray[0]);
            NodeType publisherNodeType = NodeType.valueOf(channelArray[1]);
            String serverAddress = channelArray[3];
            updatePublisherLastConnectionTime(serverAddress);
            publisherNodeTypeToMessageTypesMap.get(publisherNodeType).forEach(messageType -> {

                if (messageType.equals(propagatedMessageType)) {
                    handleMessageData(message, propagatedMessageType, publisherNodeType);
                }
            });
        }
    }

    private void updatePublisherLastConnectionTime(String publisherAddressAndPort) {
        ConnectedNodeData connectedNodeData = connectedNodes.get(publisherAddressAndPort);
        if (connectedNodeData != null) {
            connectedNodeData.setLastConnectionTime(Instant.now());
        }
    }

    private void handleMessageData(byte[] message, Class<? extends IPropagatable> propagatedMessageType, NodeType publisherNodeType) {
        try {
            IEntity messageData = serializer.deserialize(message);
            subscriberHandler.get(propagatedMessageType.getSimpleName()).apply(publisherNodeType).accept(messageData);
        } catch (ClassCastException e) {
            log.error("Invalid request received: " + e.getMessage());
        } catch (Exception e) {
            log.error("ZMQ subscriber message handler error", e);
        }
    }

    @Override
    public void connectAndSubscribeToServer(String publisherAddressAndPort, NodeType publisherNodeType) {
        log.info("ZeroMQ subscriber connecting to address {}", publisherAddressAndPort);
        if (propagationReceiver.connect(publisherAddressAndPort)) {
            log.info("Subscriber connected to server {} of node type {}", publisherAddressAndPort, publisherNodeType);
            subscribeAll(publisherAddressAndPort, publisherNodeType);
            connectedNodes.put(publisherAddressAndPort, new ConnectedNodeData(publisherNodeType, Instant.now()));

        } else {
            log.error("Unable to connect to server {} of node type {}", publisherAddressAndPort, publisherNodeType);
        }
    }

    private void subscribeAll(String publisherAddressAndPort, NodeType publisherNodeType) {
        propagationReceiver.subscribe("HeartBeat " + publisherAddressAndPort);
        publisherNodeTypeToMessageTypesMap.get(publisherNodeType).forEach(messageType ->
        {
            String channel = Channel.getChannelString(messageType, publisherNodeType, subscriberNodeType, publisherAddressAndPort);
            if (propagationReceiver.subscribe(channel)) {
                log.info("Subscribed to server {} and channel {}", publisherAddressAndPort, channel);
            } else {
                log.error("Subscription failed for server {} and channel {}", publisherAddressAndPort, channel);
            }
        });
    }

    @Override
    public void disconnect(String publisherAddressAndPort, NodeType publisherNodeType) {
        if (propagationReceiver.disconnect(publisherAddressAndPort)) {
            log.info("Subscriber disconnected from server {} of node type {}", publisherAddressAndPort, publisherNodeType);
            connectedNodes.remove(publisherAddressAndPort);
        } else {
            log.info("Subscriber failed disconnection from server {} of node type {}", publisherAddressAndPort, publisherNodeType);
        }
    }

    private void unsubscribeAll(String publisherAddressAndPort, NodeType publisherNodeType) {
        propagationReceiver.unsubscribe("HeartBeat " + publisherAddressAndPort);
        publisherNodeTypeToMessageTypesMap.get(publisherNodeType).forEach(messageType ->
        {
            String channel = Channel.getChannelString(messageType, publisherNodeType, subscriberNodeType, publisherAddressAndPort);
            if (propagationReceiver.unsubscribe(channel)) {
                log.info("Unsubscribed from server {} and channel {}", publisherAddressAndPort, channel);
            } else {
                log.error("UnSubscription failed from server {} and channel {}", publisherAddressAndPort, channel);
            }
        });
    }


    @Scheduled(initialDelay = INITIAL_DELAY, fixedDelay = FIXED_DELAY)
    public void reconnectToPublisher() {
        connectedNodes.forEach((serverAddress, connectedNodeData) -> {
            if (Duration.between(connectedNodeData.getLastConnectionTime(), Instant.now()).toMillis() > HEARTBEAT_INTERVAL) {
                log.info("Publisher heartbeat message timeout: server = {}, lastHeartBeat = {}", serverAddress, connectedNodeData.getLastConnectionTime());
                unsubscribeAll(serverAddress, connectedNodeData.getNodeType());
                connectAndSubscribeToServer(serverAddress, connectedNodeData.getNodeType());
            }
        });
    }

    @Override
    public int getMessageQueueSize() {
        return messageQueue.size();
    }

    @Override
    public void shutdown() {
        try {
            if (propagationReceiver != null) {
                log.info("Shutting down {}", this.getClass().getSimpleName());
                zeroMQContext.term();
                propagationReceiverThread.interrupt();
                propagationReceiverThread.join();
                messagesQueueHandlerThread.interrupt();
                messagesQueueHandlerThread.join();
            }
        } catch (InterruptedException e) {
            log.error("Interrupted shutdown ZeroMQ subscriber");
            Thread.currentThread().interrupt();
        }
    }
}