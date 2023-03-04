package io.coti.basenode.communication;

import io.coti.basenode.communication.data.ZeroMQMessageData;
import io.coti.basenode.communication.interfaces.IPropagationPublisher;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.data.PublisherHeartBeatData;
import io.coti.basenode.data.interfaces.IPropagatable;
import io.coti.basenode.exceptions.CotiRunTimeException;
import io.coti.basenode.exceptions.ZeroMQPublisherException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import org.zeromq.ZMQException;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.coti.basenode.services.BaseNodeServiceManager.serializer;

@Slf4j
@Service
public class ZeroMQPropagationPublisher implements IPropagationPublisher {

    private static final int HEARTBEAT_INTERVAL = 5000;
    private static final String ZMQ_PUBLISHER_HANDLER_ERROR = "ZeroMQ exception at publisher thread";
    private ZMQ.Context zeroMQContext;
    private ZMQ.Socket propagator;
    private String propagationPort;
    private ZMQ.Socket monitorSocket;
    private String monitorAddress;
    private final CountDownLatch countDownLatch = new CountDownLatch(1);
    private NodeType publisherNodeType;
    @Value("${server.ip}")
    private String publisherIp;
    private Thread publishMessageThread;
    private Thread publishHeartBeatMessageThread;
    private Thread monitorThread;
    private BlockingQueue<ZeroMQMessageData> publishMessageQueue;
    private final AtomicBoolean monitorInitialized = new AtomicBoolean(false);

    public void init(String propagationPort, NodeType publisherNodeType) {
        publishMessageQueue = new LinkedBlockingQueue<>();
        this.publisherNodeType = publisherNodeType;
        this.propagationPort = propagationPort;
        ZeroMQUtils.initDisconnectMapForSocketType(SocketType.PUB);
        init();
        log.info("ZeroMQ Publisher is up");
    }

    public void init() {
        zeroMQContext = ZeroMQContext.getZeroMQContext();
        setPublishMessageThread();
        setMonitorThread();
        setPublishHeartBeatMessageThread();
    }

    @Override
    public void initMonitor() {
        monitorInitialized.set(true);
    }

    public <T extends IPropagatable> void propagate(T toPropagate, List<NodeType> subscriberNodeTypes) {
        subscriberNodeTypes.forEach(subscriberNodeType -> propagateToNode(toPropagate, subscriberNodeType));
    }

    private <T extends IPropagatable> void propagateToNode(T toPropagate, NodeType subscriberNodeType) {
        String serverAddress = "tcp://" + publisherIp + ":" + propagationPort;
        log.debug("Propagating {} to {}", toPropagate.getHash(), Channel.getChannelString(toPropagate.getClass(), serverAddress, publisherNodeType, subscriberNodeType));
        byte[] message = serializer.serialize(toPropagate);
        if (!zeroMQContext.isClosed()) {
            publishMessageQueue.add(new ZeroMQMessageData(Channel.getChannelString(toPropagate.getClass(), serverAddress, publisherNodeType, subscriberNodeType), message));
        }
    }

    public void setPublishHeartBeatMessageThread() {
        publishHeartBeatMessageThread = new Thread(() -> {
            AtomicBoolean contextTerminated = new AtomicBoolean(false);
            while (!contextTerminated.get() && !Thread.currentThread().isInterrupted()) {
                try {
                    String serverAddress = "tcp://" + publisherIp + ":" + propagationPort;
                    publishMessageQueue.add(new ZeroMQMessageData(Channel.getChannelString(PublisherHeartBeatData.class, serverAddress), serializer.serialize(new PublisherHeartBeatData(serverAddress))));
                    Thread.sleep(HEARTBEAT_INTERVAL);
                } catch (InterruptedException e) {
                    log.info("HeartBeat Publisher thread interrupted");
                    Thread.currentThread().interrupt();
                } catch (ZMQException e) {
                    if (e.getErrorCode() == ZMQ.Error.ETERM.getCode()) {
                        log.info("Publisher context is terminated");
                        contextTerminated.set(true);
                    } else {
                        log.error(ZMQ_PUBLISHER_HANDLER_ERROR, e);
                    }
                }
            }
        }, "HEARTBEAT PUB");
        publishHeartBeatMessageThread.start();
    }

    public void setPublishMessageThread() {

        publishMessageThread = new Thread(() -> {
            propagator = zeroMQContext.socket(SocketType.PUB);
            propagator.setHWM(10000);
            try {
                monitorAddress = ZeroMQUtils.createAndStartMonitorOnSocket(propagator);
                countDownLatch.countDown();
                if (!propagator.bind("tcp://*:" + propagationPort)) {
                    throw new ZeroMQPublisherException("ZeroMQ publisher socket bind failed to propagation port " + propagationPort);
                }
                publishMessageData();
                publishRemainingMessages();
            } catch (CotiRunTimeException e) {
                log.error("ZeroMQPublisher runtime exception : ", e);
            } finally {
                ZeroMQUtils.closeSocket(propagator);
            }
        }, "PUB");
        publishMessageThread.start();
    }

    private void publishMessageData() {
        while (!ZeroMQContext.isContextTerminated() && !Thread.currentThread().isInterrupted()) {
            try {
                ZeroMQMessageData messageData = publishMessageQueue.take();
                publish(messageData);
            } catch (InterruptedException e) {
                log.info("Publisher thread interrupted");
                Thread.currentThread().interrupt();
            } catch (ZMQException e) {
                if (e.getErrorCode() == ZMQ.Error.ETERM.getCode()) {
                    log.info("Publisher context is terminated");
                    ZeroMQContext.setContextTerminated(true);
                } else {
                    log.error(ZMQ_PUBLISHER_HANDLER_ERROR, e);
                }
            }
        }
    }

    private void publish(ZeroMQMessageData messageData) {
        propagator.sendMore(messageData.getChannel().getBytes());
        propagator.send(messageData.getMessage());
    }

    private void publishRemainingMessages() {
        LinkedList<ZeroMQMessageData> remainingMessages = new LinkedList<>();
        publishMessageQueue.drainTo(remainingMessages);
        if (!remainingMessages.isEmpty()) {
            log.info("Please wait to publish {} remaining messages", remainingMessages.size());
            remainingMessages.forEach(this::publish);
        }
    }

    private void setMonitorThread() {
        monitorThread = new Thread(() -> {
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            monitorSocket = ZeroMQUtils.createAndConnectMonitorSocket(zeroMQContext, monitorAddress);
            while (!ZeroMQContext.isContextTerminated() && !Thread.currentThread().isInterrupted()) {
                try {
                    ZeroMQUtils.getServerSocketEvent(monitorSocket, SocketType.PUB, monitorInitialized);
                } catch (ZMQException e) {
                    if (e.getErrorCode() == ZMQ.Error.ETERM.getCode()) {
                        ZeroMQContext.setContextTerminated(true);
                    } else {
                        log.error("ZeroMQ exception at monitor publisher thread", e);
                    }
                }
            }
            ZeroMQUtils.closeSocket(monitorSocket);
        }, "MONITOR PUB");
        monitorThread.start();
    }

    @Override
    public int getQueueSize() {
        if (publishMessageQueue != null) {
            return publishMessageQueue.size();
        } else {
            return -1;
        }
    }

    public void shutdown() {
        if (propagator != null) {
            log.info("Shutting down {}", this.getClass().getSimpleName());
            try {
                publishMessageThread.interrupt();
                publishMessageThread.join();
                publishHeartBeatMessageThread.interrupt();
                publishHeartBeatMessageThread.join();
                monitorThread.interrupt();
                monitorThread.join();
            } catch (InterruptedException e) {
                log.error("ZeroMQPropagationPublisher interrupted", e);
                Thread.currentThread().interrupt();
            }

        }
    }
}
