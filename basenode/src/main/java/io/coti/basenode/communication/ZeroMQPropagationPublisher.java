package io.coti.basenode.communication;

import io.coti.basenode.communication.data.ZeroMQMessageData;
import io.coti.basenode.communication.interfaces.IPropagationPublisher;
import io.coti.basenode.communication.interfaces.ISerializer;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.data.PublisherHeartBeatData;
import io.coti.basenode.data.interfaces.IPropagatable;
import io.coti.basenode.exceptions.ZeroMQPublisherException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import org.zeromq.ZMQException;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
public class ZeroMQPropagationPublisher implements IPropagationPublisher {

    private static final int HEARTBEAT_INTERVAL = 5000;
    private static final String ZMQ_PUBLISHER_HANDLER_ERROR = "ZeroMQ exception at publisher thread";
    private ZMQ.Context zeroMQContext;
    private ZMQ.Socket propagator;
    private String propagationPort;
    private ZMQ.Socket monitorSocket;
    private NodeType publisherNodeType;
    @Value("${server.ip}")
    private String publisherIp;
    private Thread publishMessageThread;
    private Thread publishHeartBeatMessageThread;
    private Thread monitorThread;
    private BlockingQueue<ZeroMQMessageData> publishMessageQueue;
    @Autowired
    private ISerializer serializer;
    private final AtomicBoolean monitorInitialized = new AtomicBoolean(false);

    public void init(String propagationPort, NodeType publisherNodeType) {
        publishMessageQueue = new LinkedBlockingQueue<>();
        this.publisherNodeType = publisherNodeType;
        this.propagationPort = propagationPort;
        init();
        log.info("ZeroMQ Publisher is up");
    }

    public void init() {
        zeroMQContext = ZMQ.context(1);
        propagator = zeroMQContext.socket(SocketType.PUB);
        monitorSocket = ZeroMQUtils.createAndConnectMonitorSocket(zeroMQContext, propagator);
        propagator.setHWM(10000);
        if (!propagator.bind("tcp://*:" + propagationPort)) {
            throw new ZeroMQPublisherException("ZeroMQ publisher socket bind failed to propagation port " + propagationPort);
        }
        setMonitorThread();
        setPublishHeartBeatMessageThread();
        setPublishMessageThread();
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
                    publish(new ZeroMQMessageData(Channel.getChannelString(PublisherHeartBeatData.class, serverAddress), serializer.serialize(new PublisherHeartBeatData(serverAddress))));
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
            boolean contextTerminated = false;
            while (!contextTerminated && !Thread.currentThread().isInterrupted()) {
                try {
                    ZeroMQMessageData messageData = publishMessageQueue.take();
                    publish(messageData);
                } catch (InterruptedException e) {
                    log.info("Publisher thread interrupted");
                    Thread.currentThread().interrupt();
                } catch (ZMQException e) {
                    if (e.getErrorCode() == ZMQ.Error.ETERM.getCode()) {
                        log.info("Publisher context is terminated");
                        contextTerminated = true;
                    } else {
                        log.error(ZMQ_PUBLISHER_HANDLER_ERROR, e);
                    }
                }
            }
            publishRemainingMessages();
        }, "PUB");
        publishMessageThread.start();
    }

    private void publish(ZeroMQMessageData messageData) {
        synchronized (this) {
            propagator.sendMore(messageData.getChannel().getBytes());
            propagator.send(messageData.getMessage());
        }
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
            AtomicBoolean contextTerminated = new AtomicBoolean(false);
            while (!contextTerminated.get() && !Thread.currentThread().isInterrupted()) {
                try {
                    ZeroMQUtils.getServerSocketEvent(monitorSocket, SocketType.PUB, monitorInitialized, contextTerminated);
                } catch (ZMQException e) {
                    if (e.getErrorCode() == ZMQ.Error.ETERM.getCode()) {
                        contextTerminated.set(true);
                    } else {
                        log.error("ZeroMQ exception at monitor publisher thread", e);
                    }
                } catch (Exception e) {
                    log.error("Exception at monitor publisher thread", e);
                }
            }
            monitorSocket.close();
        }, "MONITOR PUB");
        monitorThread.start();
    }

    public void shutdown() {
        if (propagator != null) {
            log.info("Shutting down {}", this.getClass().getSimpleName());
            propagator.setLinger(1000);
            propagator.close();
            zeroMQContext.term();
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
