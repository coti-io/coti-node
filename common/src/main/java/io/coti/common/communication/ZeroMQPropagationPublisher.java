package io.coti.common.communication;

import io.coti.common.communication.interfaces.IPropagationPublisher;
import io.coti.common.communication.interfaces.ISerializer;
import io.coti.common.data.interfaces.IEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.zeromq.ZMQ;

import javax.annotation.PostConstruct;

@Slf4j
@Service
public class ZeroMQPropagationPublisher implements IPropagationPublisher {
    @Value("${propagation.port}")
    private String propagationPort;
    private ZMQ.Context zeroMQContext;
    private ZMQ.Socket propagator;

    @Autowired
    private ISerializer serializer;

    @PostConstruct
    private void init() {
        zeroMQContext = ZMQ.context(1);
        propagator = zeroMQContext.socket(ZMQ.PUB);
        propagator.bind("tcp://*:" + propagationPort);
    }

    public <T extends IEntity> void propagate(T toPropagate, String channel) {
        log.info("Propagating transaction {} to {}", toPropagate.getHash().toHexString(), channel);
        byte[] message = serializer.serialize(toPropagate);
        propagator.sendMore(channel.getBytes());
        propagator.send(message);
    }
}