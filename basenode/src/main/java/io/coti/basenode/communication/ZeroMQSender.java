package io.coti.basenode.communication;

import io.coti.basenode.communication.interfaces.ISender;
import io.coti.basenode.communication.interfaces.ISerializer;
import io.coti.basenode.data.interfaces.IPropagatable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.zeromq.ZMQ;
import org.zeromq.ZMQException;

import javax.annotation.PostConstruct;
import java.util.List;
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
    }

    @Override
    public void init(List<String> receivingServerAddresses) {
        zeroMQContext = ZMQ.context(1);
        receivingAddressToSenderSocketMapping = new ConcurrentHashMap<>();
        receivingServerAddresses.forEach(this::initializeSenderSocket);
    }

    @Override
    public <T extends IPropagatable> void send(T toSend, String address) {
        byte[] message = serializer.serialize(toSend);
        synchronized (zeroMQContext) {
            try {
                receivingAddressToSenderSocketMapping.get(address).sendMore(toSend.getClass().getName());
                receivingAddressToSenderSocketMapping.get(address).send(message);
            } catch (ZMQException exception) {
                return;
            }
        }
    }

    private void initializeSenderSocket(String address) {
        ZMQ.Socket sender = zeroMQContext.socket(ZMQ.DEALER);
        ZeroMQUtils.bindToRandomPort(sender);
        sender.connect(address);
        receivingAddressToSenderSocketMapping.putIfAbsent(address, sender);
    }
}