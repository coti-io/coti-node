package io.coti.basenode.communication;

import io.coti.basenode.communication.data.ConnectedNodeData;
import io.coti.basenode.communication.data.ReconnectMonitorData;
import io.coti.basenode.communication.data.ZeroMQMessageData;
import io.coti.basenode.communication.interfaces.IPropagationSubscriber;
import io.coti.basenode.communication.interfaces.ISerializer;
import io.coti.basenode.communication.interfaces.ISubscriberHandler;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.data.PublisherHeartBeatData;
import io.coti.basenode.data.interfaces.IPropagatable;
import io.coti.basenode.exceptions.CotiRunTimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import org.zeromq.ZMQException;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
public class ZeroMQSubscriber implements IPropagationSubscriber {

    private static final int HEARTBEAT_INTERVAL = 10000;
    private static final int INITIAL_DELAY = 5000;
    private static final int FIXED_DELAY = 5000;
    private static final String ZMQ_SUBSCRIBER_CONTEXT_TERMINATED = "ZeroMQ subscriber context terminated";
    private static final String ZMQ_SUBSCRIBER_HANDLER_ERROR = "ZMQ subscriber message handler task error";
    private ZMQ.Context zeroMQContext;
    private SocketType socketType;
    private ZMQ.Socket propagationSubscriber;
    private ZMQ.Socket monitorSocket;
    private final Map<String, ConnectedNodeData> connectedNodes = new ConcurrentHashMap<>();
    private Thread propagationSubscriberThread;
    private Thread monitorThread;
    private Thread monitorReconnectThread;
    private final Map<String, ReconnectMonitorData> addressToReconnectMonitorMap = new ConcurrentHashMap<>();
    @Autowired
    private ISerializer serializer;
    private EnumMap<NodeType, List<Class<? extends IPropagatable>>> publisherNodeTypeToMessageTypesMap;
    private final Map<String, Thread> queueNameToThreadMap = new HashMap<>();
    private NodeType subscriberNodeType;
    @Autowired
    private ISubscriberHandler subscriberHandler;
    private final AtomicBoolean monitorInitialized = new AtomicBoolean(false);

    @Override
    public void init() {
        initSockets();
        BlockingQueue<ZeroMQMessageData> messageQueue = ZeroMQSubscriberQueue.HEARTBEAT.getQueue();
        queueNameToThreadMap.put(ZeroMQSubscriberQueue.HEARTBEAT.name(), new Thread(() -> this.handleMessagesQueueTask(messageQueue), ZeroMQSubscriberQueue.HEARTBEAT.name() + " SUB"));
        subscriberHandler.init();
    }

    private void initSockets() {
        zeroMQContext = ZMQ.context(1);
        socketType = SocketType.SUB;
        propagationSubscriber = zeroMQContext.socket(socketType);
        propagationSubscriber.setHWM(10000);
        propagationSubscriber.setLinger(100);
        monitorSocket = ZeroMQUtils.createAndConnectMonitorSocket(zeroMQContext, propagationSubscriber);
        startMonitorThread();
        ZeroMQUtils.bindToRandomPort(propagationSubscriber);
    }

    @Override
    public void setSubscriberNodeType(NodeType subscriberNodeType) {
        this.subscriberNodeType = subscriberNodeType;
    }

    @Override
    public void setPublisherNodeTypeToMessageTypesMap(EnumMap<NodeType, List<Class<? extends IPropagatable>>> publisherNodeTypeToMessageTypesMap) {
        this.publisherNodeTypeToMessageTypesMap = publisherNodeTypeToMessageTypesMap;
        publisherNodeTypeToMessageTypesMap.forEach(((nodeType, classes) -> classes.forEach(messageType -> {
            ZeroMQSubscriberQueue queueEnum = ZeroMQSubscriberQueue.getQueueEnum(messageType);
            queueNameToThreadMap.putIfAbsent(queueEnum.toString(), new Thread(() -> this.handleMessagesQueueTask(queueEnum.getQueue()), queueEnum.name() + " SUB"));
        })));
    }

    @Override
    public void startListening() {
        startPropagationSubscriberThread();
    }

    private void startPropagationSubscriberThread() {
        propagationSubscriberThread = new Thread(() -> {
            boolean contextTerminated = false;
            while (!contextTerminated && !Thread.currentThread().isInterrupted()) {
                try {
                    addToMessageQueue();
                } catch (ZMQException e) {
                    if (e.getErrorCode() == ZMQ.Error.ETERM.getCode()) {
                        log.info(ZMQ_SUBSCRIBER_CONTEXT_TERMINATED);
                        contextTerminated = true;
                    } else if (e.getErrorCode() == ZMQ.Error.EINTR.getCode()) {
                        log.info("ZeroMQ subscriber thread is interrupted");
                        Thread.currentThread().interrupt();
                    } else {
                        ZMQ.Error zmqError = ZMQ.Error.findByCode(e.getErrorCode());
                        log.error("ZeroMQ exception at subscriber thread: {} , {}", zmqError, zmqError.getMessage());
                    }
                } catch (Exception e) {
                    log.error("Error at subscriber thread", e);
                }
            }
            propagationSubscriber.close();
        }, "SUB");
        propagationSubscriberThread.start();
    }

    private void startMonitorThread() {
        monitorThread = new Thread(() -> {
            AtomicBoolean contextTerminated = new AtomicBoolean(false);
            while (!contextTerminated.get() && !Thread.currentThread().isInterrupted()) {
                try {
                    getEvent(contextTerminated);
                } catch (ZMQException e) {
                    if (e.getErrorCode() == ZMQ.Error.ETERM.getCode()) {
                        log.info(ZMQ_SUBSCRIBER_CONTEXT_TERMINATED);
                        contextTerminated.set(true);
                    } else {
                        log.error("ZeroMQ exception at monitor subscriber thread", e);
                    }
                } catch (Exception e) {
                    log.error("Exception at monitor subscriber thread", e);
                }
            }
            monitorSocket.close();
        }, "MONITOR SUB");
        monitorThread.start();
    }

    private void getEvent(AtomicBoolean contextTerminated) {
        if (monitorReconnectThread == null) {
            monitorReconnectThread = ZeroMQUtils.getMonitorReconnectThread(addressToReconnectMonitorMap, socketType);
        }
        ZeroMQUtils.getClientServerEvent(monitorSocket, socketType, monitorInitialized, contextTerminated, addressToReconnectMonitorMap, this::getNodeTypeByAddress);
    }

    private NodeType getNodeTypeByAddress(String address) {
        return Optional.ofNullable(connectedNodes.get(address)).map(ConnectedNodeData::getNodeType).orElse(null);
    }

    private void addToMessageQueue() throws ClassNotFoundException {
        try {
            String channel = propagationSubscriber.recvStr();
            log.debug("Received a new message on channel: {}", channel);
            String[] channelArray = channel.split("-");
            Class<? extends IPropagatable> propagatedMessageType = (Class<? extends IPropagatable>) Class.forName(channelArray[0]);
            byte[] message = propagationSubscriber.recv();
            ZeroMQSubscriberQueue.getQueue(propagatedMessageType).put(new ZeroMQMessageData(channel, message));
        } catch (InterruptedException e) {
            log.info("ZMQ subscriber propagation receiver interrupted");
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void initPropagationHandler() {
        queueNameToThreadMap.values().forEach(Thread::start);
    }

    @Override
    public void initMonitor() {
        monitorInitialized.set(true);
    }

    private void handleMessagesQueueTask(BlockingQueue<ZeroMQMessageData> messageQueue) {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                ZeroMQMessageData zeroMQMessageData = messageQueue.take();
                log.debug("ZMQ message arrived: {}", zeroMQMessageData);
                propagationProcess(zeroMQMessageData);
            } catch (InterruptedException e) {
                log.info("ZMQ subscriber message handler interrupted");
                Thread.currentThread().interrupt();
            } catch (CotiRunTimeException e) {
                log.error(ZMQ_SUBSCRIBER_HANDLER_ERROR);
                e.logMessage();
            } catch (Exception e) {
                log.error(ZMQ_SUBSCRIBER_HANDLER_ERROR, e);
            }
        }
        LinkedList<ZeroMQMessageData> remainingMessages = new LinkedList<>();
        messageQueue.drainTo(remainingMessages);
        if (!remainingMessages.isEmpty()) {
            log.info("Please wait to process {} remaining messages", remainingMessages.size());
            remainingMessages.forEach(zeroMQMessageData -> {
                try {
                    propagationProcess(zeroMQMessageData);
                } catch (CotiRunTimeException e) {
                    log.error(ZMQ_SUBSCRIBER_HANDLER_ERROR);
                    e.logMessage();
                } catch (Exception e) {
                    log.error(ZMQ_SUBSCRIBER_HANDLER_ERROR, e);
                }
            });
        }

    }

    private void propagationProcess(ZeroMQMessageData zeroMQMessageData) throws ClassNotFoundException {
        String channel = zeroMQMessageData.getChannel();
        byte[] message = zeroMQMessageData.getMessage();
        IPropagatable messageData = serializer.deserialize(message);
        String[] channelArray = channel.split("-");
        Class<? extends IPropagatable> propagatedMessageType = (Class<? extends IPropagatable>) Class.forName(channelArray[0]);
        if (propagatedMessageType.equals(PublisherHeartBeatData.class)) {
            String serverAddress = ((PublisherHeartBeatData) messageData).getServerAddress();
            updatePublisherLastConnectionTime(serverAddress);
        } else {
            String serverAddress = channelArray[1];
            NodeType publisherNodeType = NodeType.valueOf(channelArray[2]);
            updatePublisherLastConnectionTime(serverAddress);
            publisherNodeTypeToMessageTypesMap.get(publisherNodeType).forEach(messageType -> {

                if (messageType.equals(propagatedMessageType)) {
                    handleMessageData(messageData, propagatedMessageType, publisherNodeType);
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

    private void handleMessageData(IPropagatable messageData, Class<? extends IPropagatable> propagatedMessageType, NodeType publisherNodeType) {
        try {
            subscriberHandler.get(propagatedMessageType.getSimpleName()).apply(publisherNodeType).accept(messageData);
        } catch (ClassCastException e) {
            log.error("Invalid request received: " + e.getMessage());
        } catch (Exception e) {
            log.error("ZMQ subscriber message handler error", e);
        }
    }

    @Override
    public void connectAndSubscribeToServer(String publisherAddressAndPort, NodeType publisherNodeType) {
        connectAndSubscribeToServer(publisherAddressAndPort, publisherNodeType, true);
    }

    private void connectAndSubscribeToServer(String publisherAddressAndPort, NodeType publisherNodeType, boolean info) {
        log.info("ZeroMQ subscriber connecting to address {}", publisherAddressAndPort);
        if (propagationSubscriber.connect(publisherAddressAndPort)) {
            log.info("Subscriber connected to server {} of node type {}", publisherAddressAndPort, publisherNodeType);
            subscribeAll(publisherAddressAndPort, publisherNodeType, info);
            connectedNodes.put(publisherAddressAndPort, new ConnectedNodeData(publisherNodeType, Instant.now()));

        } else {
            log.error("Unable to connect to server {} of node type {}", publisherAddressAndPort, publisherNodeType);
        }
    }

    private void subscribeAll(String publisherAddressAndPort, NodeType publisherNodeType, boolean info) {
        propagationSubscriber.subscribe(Channel.getChannelString(PublisherHeartBeatData.class, publisherAddressAndPort));
        publisherNodeTypeToMessageTypesMap.get(publisherNodeType).forEach(messageType ->
        {
            String channel = Channel.getChannelString(messageType, publisherAddressAndPort, publisherNodeType, subscriberNodeType);
            if (propagationSubscriber.subscribe(channel)) {
                if (info) {
                    log.info("Subscribed to server {} and channel {}", publisherAddressAndPort, channel);
                }
            } else {
                log.error("Subscription failed for server {} and channel {}", publisherAddressAndPort, channel);
            }
        });
    }

    @Override
    public void disconnect(String publisherAddressAndPort, NodeType publisherNodeType) {
        if (propagationSubscriber.disconnect(publisherAddressAndPort)) {
            log.info("Subscriber disconnected from server {} of node type {}", publisherAddressAndPort, publisherNodeType);
            connectedNodes.remove(publisherAddressAndPort);
            ZeroMQUtils.removeFromReconnectMonitor(addressToReconnectMonitorMap, publisherAddressAndPort);
        } else {
            log.info("Subscriber failed disconnection from server {} of node type {}", publisherAddressAndPort, publisherNodeType);
        }
    }

    private void unsubscribeAll(String publisherAddressAndPort, NodeType publisherNodeType) {
        propagationSubscriber.unsubscribe(Channel.getChannelString(PublisherHeartBeatData.class, publisherAddressAndPort));
        publisherNodeTypeToMessageTypesMap.get(publisherNodeType).forEach(messageType ->
        {
            String channel = Channel.getChannelString(messageType, publisherAddressAndPort, publisherNodeType, subscriberNodeType);
            if (propagationSubscriber.unsubscribe(channel)) {
                log.debug("Unsubscribed from server {} and channel {}", publisherAddressAndPort, channel);
            } else {
                log.error("UnSubscription failed from server {} and channel {}", publisherAddressAndPort, channel);
            }
        });
    }


    @Scheduled(initialDelay = INITIAL_DELAY, fixedDelay = FIXED_DELAY)
    public void reconnectToPublisher() {
        connectedNodes.forEach((serverAddress, connectedNodeData) -> {
            if (Duration.between(connectedNodeData.getLastConnectionTime(), Instant.now()).toMillis() > HEARTBEAT_INTERVAL) {
                NodeType nodeType = connectedNodeData.getNodeType();
                log.error("Publisher heartbeat message timeout: server = {}, nodeType = {}, lastHeartBeat = {}", serverAddress, nodeType, connectedNodeData.getLastConnectionTime());
                unsubscribeAll(serverAddress, nodeType);
                connectAndSubscribeToServer(serverAddress, nodeType, false);
            }
        });
    }

    @Override
    public int getMessageQueueSize(ZeroMQSubscriberQueue zeroMQSubscriberQueue) {
        return zeroMQSubscriberQueue.getQueue().size();
    }

    @Override
    public void shutdown() {
        try {
            if (propagationSubscriber != null) {
                log.info("Shutting down {}", this.getClass().getSimpleName());
                zeroMQContext.term();
                propagationSubscriberThread.interrupt();
                propagationSubscriberThread.join();
                monitorThread.interrupt();
                monitorThread.join();
                if (monitorReconnectThread != null) {
                    monitorReconnectThread.interrupt();
                    monitorReconnectThread.join();
                }
                queueNameToThreadMap.values().forEach(thread -> {
                    try {
                        thread.interrupt();
                        thread.join();
                    } catch (InterruptedException e) {
                        log.error("Interrupted shutdown ZeroMQ subscriber");
                        Thread.currentThread().interrupt();
                    }
                });
            }
        } catch (InterruptedException e) {
            log.error("Interrupted shutdown ZeroMQ subscriber");
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("Shutdown error ZeroMQ subscriber", e);
        }
    }
}
