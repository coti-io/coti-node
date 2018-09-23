package io.coti.basenode.communication;

import io.coti.basenode.communication.interfaces.IPropagationPublisher;
import io.coti.basenode.communication.interfaces.ISerializer;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.data.interfaces.IEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.zeromq.ZMQ;

import javax.annotation.PreDestroy;
import java.util.List;

@Slf4j
@Service
public class ZeroMQPropagationPublisher implements IPropagationPublisher {
    private ZMQ.Context zeroMQContext;
    private ZMQ.Socket propagator;
    private String propagationPort;
    @Value("${server.ip}")
    private String publisherIp;
    private final int HEARTBEAT_INTERVAL = 5000;
    private final int INITIAL_DELAY = 1000;

    @Autowired
    private ISerializer serializer;

    public void init(String propagationPort) {
        zeroMQContext = ZMQ.context(1);
        propagator = zeroMQContext.socket(ZMQ.PUB);
        propagator.setHWM(10000);
        this.propagationPort = propagationPort;
        propagator.bind("tcp://*:" + propagationPort);
    }

    public <T extends IEntity> void propagate(T toPropagate, List<NodeType> subscriberNodeTypes) {
        synchronized (propagator) {
            subscriberNodeTypes.forEach(nodeType -> {
                log.debug("Propagating {} to {}", toPropagate.getHash(), Channel.getChannelString(toPropagate.getClass(), nodeType));
                byte[] message = serializer.serialize(toPropagate);
                propagator.sendMore(Channel.getChannelString(toPropagate.getClass(), nodeType).getBytes());
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

    @PreDestroy
    public void shutdown() {
        log.info("Shutting down ZeroMQ publisher");
        if (propagator != null) {
            propagator.unbind("tcp://*:" + propagationPort);
            propagator.close();
            zeroMQContext.close();
        }
    }
}