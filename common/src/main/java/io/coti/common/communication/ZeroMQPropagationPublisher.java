package io.coti.common.communication;

import io.coti.common.Channel;
import io.coti.common.NodeType;
import io.coti.common.communication.interfaces.IPropagationPublisher;
import io.coti.common.communication.interfaces.ISerializer;
import io.coti.common.data.interfaces.IEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.zeromq.ZMQ;

import javax.annotation.PostConstruct;
import java.util.List;

@Slf4j
@Service
public class ZeroMQPropagationPublisher implements IPropagationPublisher {
    private ZMQ.Context zeroMQContext;
    private ZMQ.Socket propagator;

    @Autowired
    private ISerializer serializer;

    public void init(String propagationPort) {
        zeroMQContext = ZMQ.context(1);
        propagator = zeroMQContext.socket(ZMQ.PUB);
        propagator.setHWM(10000);
        propagator.bind("tcp://*:" + propagationPort);
    }

    public <T extends IEntity> void propagate(T toPropagate, List<NodeType> subscriberNodeTypes) {
        synchronized (propagator) {
            subscriberNodeTypes.forEach(nodeType -> {
                log.debug("Propagating {} to {}", toPropagate.getHash().toHexString(), Channel.getChannelString(toPropagate.getClass(), nodeType));
                byte[] message = serializer.serialize(toPropagate);
                propagator.sendMore(Channel.getChannelString(toPropagate.getClass(), nodeType).getBytes());
                propagator.send(message);
            });
        }
    }
}