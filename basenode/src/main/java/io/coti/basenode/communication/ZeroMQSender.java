package io.coti.basenode.communication;

import io.coti.basenode.communication.interfaces.ISender;
import io.coti.basenode.communication.interfaces.ISerializer;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.data.interfaces.IEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.zeromq.ZMQ;
import org.zeromq.ZMQException;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class ZeroMQSender implements ISender {
    private ZMQ.Context zeroMQContext;
    private Map<String, ZMQ.Socket> receivingAddressToSenderSocketMapping;

    @Autowired
    private ISerializer serializer;

    @PostConstruct
    private void init() {
        zeroMQContext = ZMQ.context(1);
        receivingAddressToSenderSocketMapping = new ConcurrentHashMap<>();
    }

    @Override
    public void addAddress(String receivingServerAddress) {
        initializeSenderSocket(receivingServerAddress);
    }

    @Override
    public <T extends IEntity> void send(T toSend, String address) {
        byte[] message = serializer.serialize(toSend);
        synchronized (zeroMQContext) {
            try {
                receivingAddressToSenderSocketMapping.get(address).sendMore(toSend.getClass().getName());
                receivingAddressToSenderSocketMapping.get(address).send(message);
                log.info("Message {} was sent to {}", toSend, toSend.getClass().getName());
            } catch (ZMQException exception) {
                log.error("Exception in sending", exception);
                return;
            }
        }
    }

    @Override
    public void removeAddress(String receivingFullAddress, NodeType nodeType) {
        ZMQ.Socket sender = receivingAddressToSenderSocketMapping.get(receivingFullAddress);
        if(sender != null){
            log.error("{} with address  {} is about to be removed from sending to zmq", nodeType, receivingFullAddress);
            if(!sender.unsubscribe(receivingFullAddress)) {
                log.error("{} with address  {} sender failed to be removed from zmq", nodeType, receivingFullAddress);
            }
            receivingAddressToSenderSocketMapping.remove(receivingFullAddress);
        }
        else{
            log.error("{} with address  {} was about to be removed but doesn't exit in receivingAddressToSenderSocketMapping ",
                   nodeType ,receivingFullAddress);
        }

    }

    private void initializeSenderSocket(String addressAndPort) {
        ZMQ.Socket sender = zeroMQContext.socket(ZMQ.DEALER);
        ZeroMQUtils.bindToRandomPort(sender);
        sender.connect(addressAndPort);
        receivingAddressToSenderSocketMapping.putIfAbsent(addressAndPort, sender);
    }
}