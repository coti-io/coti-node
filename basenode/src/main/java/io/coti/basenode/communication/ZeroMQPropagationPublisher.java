package io.coti.basenode.communication;

import io.coti.basenode.communication.data.ZeroMQMessageData;
import io.coti.basenode.communication.interfaces.IPropagationPublisher;
import io.coti.basenode.communication.interfaces.ISerializer;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.data.interfaces.IPropagatable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.zeromq.ZMQ;
import org.zeromq.ZMQException;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
@Service
public class ZeroMQPropagationPublisher implements IPropagationPublisher {
    private ZMQ.Context zeroMQContext;
    private ZMQ.Socket propagator;
    private String propagationPort;
    private NodeType publisherNodeType;
    @Value("${server.ip}")
    private String publisherIp;
    private final int HEARTBEAT_INTERVAL = 5000;
    private final int INITIAL_DELAY = 1000;
    private Thread publishMessageThread;
    private BlockingQueue<ZeroMQMessageData> publishMessageQueue;
    boolean contextTerminated;

    @Autowired
    private ISerializer serializer;

    public void init(String propagationPort, NodeType publisherNodeType) {
        publishMessageQueue = new LinkedBlockingQueue<>();
        this.publisherNodeType = publisherNodeType;
        this.propagationPort = propagationPort;
        init();
        contextTerminated = false;
        log.info("ZeroMQ Publisher is up");
    }

    public void init() {
        zeroMQContext = ZMQ.context(1);
        propagator = zeroMQContext.socket(ZMQ.PUB);
        propagator.setHWM(10000);
        propagator.bind("tcp://*:" + propagationPort);
        setPublishMessageThread();
    }

    public <T extends IPropagatable> void propagate(T toPropagate, List<NodeType> subscriberNodeTypes) {

        subscriberNodeTypes.forEach(subscriberNodeType -> propagateToNode(toPropagate, subscriberNodeType));

    }

    private <T extends IPropagatable> void propagateToNode(T toPropagate, NodeType subscriberNodeType) {
        String serverAddress = "tcp://" + publisherIp + ":" + propagationPort;
        log.debug("Propagating {} to {}", toPropagate.getHash(), Channel.getChannelString(toPropagate.getClass(), publisherNodeType, subscriberNodeType, serverAddress));
        byte[] message = serializer.serialize(toPropagate);
        if (!zeroMQContext.isClosed()) {
            publishMessageQueue.add(new ZeroMQMessageData(Channel.getChannelString(toPropagate.getClass(), publisherNodeType, subscriberNodeType, serverAddress), message));
        }
    }

    @Scheduled(initialDelay = INITIAL_DELAY, fixedDelay = HEARTBEAT_INTERVAL)
    public void propagateHeartBeatMessage() {
        if (propagator != null) {
            String serverAddress = "tcp://" + publisherIp + ":" + propagationPort;
            if (!zeroMQContext.isClosed()) {
                publishMessageQueue.add(new ZeroMQMessageData(new String("HeartBeat " + serverAddress), serverAddress.getBytes()));
            }
        }

    }

    public void setPublishMessageThread() {

        publishMessageThread = new Thread(() -> {
            while (!contextTerminated && !Thread.currentThread().isInterrupted()) {
                try {
                    ZeroMQMessageData messageData = publishMessageQueue.take();
                    propagator.sendMore(messageData.getChannel().getBytes());
                    propagator.send(messageData.getMessage());
                } catch (InterruptedException e) {
                    log.info("Publisher thread interrupted");
                    Thread.currentThread().interrupt();
                } catch (ZMQException e) {
                    if (e.getErrorCode() == ZMQ.Error.ETERM.getCode()) {
                        log.info("Publisher context is terminated");
                        contextTerminated = true;
                    } else {
                        log.error("ZeroMQ exception at publisher thread", e);
                    }
                }
            }
            LinkedList<ZeroMQMessageData> remainingMessages = new LinkedList<>();
            publishMessageQueue.drainTo(remainingMessages);
            if (remainingMessages.size() != 0) {
                log.info("Please wait to publish {} remaining messages", remainingMessages.size());
                remainingMessages.forEach(messageData -> {
                    propagator.sendMore(messageData.getChannel().getBytes());
                    propagator.send(messageData.getMessage());
                });
            }
        });
        publishMessageThread.start();
    }

    public void shutdown() {
        if (propagator != null) {
            log.info("Shutting down {}", this.getClass().getSimpleName());
            propagator.setLinger(1000);
            propagator.close();
            zeroMQContext.term();
            try {
                Thread.sleep(1000);
                if (contextTerminated == false) {
                    publishMessageThread.interrupt();
                    publishMessageThread.join();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }
}