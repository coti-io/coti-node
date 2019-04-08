package io.coti.basenode.communication;

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

import java.util.List;

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

    @Autowired
    private ISerializer serializer;

    public void init(String propagationPort, NodeType publisherNodeType) {
        this.publisherNodeType = publisherNodeType;
        zeroMQContext = ZMQ.context(1);
        propagator = zeroMQContext.socket(ZMQ.PUB);
        propagator.setHWM(10000);
        this.propagationPort = propagationPort;
        propagator.bind("tcp://*:" + propagationPort);
        log.info("ZeroMQ Publisher is up");
    }

    public <T extends IPropagatable> void propagate(T toPropagate, List<NodeType> subscriberNodeTypes) {
        synchronized (propagator) {
            subscriberNodeTypes.forEach(subscriberNodeType -> {
                String serverAddress = "tcp://" + publisherIp + ":" + propagationPort;
                log.debug("Propagating {} to {}", toPropagate.getHash(), Channel.getChannelString(toPropagate.getClass(), publisherNodeType, subscriberNodeType, serverAddress));
                byte[] message = serializer.serialize(toPropagate);
                propagator.sendMore(Channel.getChannelString(toPropagate.getClass(), publisherNodeType, subscriberNodeType, serverAddress).getBytes());
                propagator.send(message);
            });
        }
    }

    @Scheduled(initialDelay = INITIAL_DELAY, fixedDelay = HEARTBEAT_INTERVAL)
    public void propagateHeartBeatMessage() {
        if (propagator != null) {
            synchronized (propagator) {
                String serverAddress = "tcp://" + publisherIp + ":" + propagationPort;
                propagator.sendMore(new String("HeartBeat " + serverAddress).getBytes());
                propagator.send(serverAddress.getBytes());
            }
        }
    }

    public void shutdown() {
        if (propagator != null) {
            log.info("Shutting down {}", this.getClass().getSimpleName());
            propagator.unbind("tcp://*:" + propagationPort);
            propagator.close();
            zeroMQContext.close();
        }
    }
}