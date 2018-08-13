package io.coti.common.communication;

import io.coti.common.communication.ZeroMQUtils;
import io.coti.common.communication.interfaces.ISender;
import io.coti.common.communication.interfaces.ISerializer;
import io.coti.common.data.interfaces.IEntity;
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
    public <T extends IEntity> void send(T toSend, String address) {
        if (!receivingAddressToSenderSocketMapping.containsKey(address)) {
            initializeSenderSocket(address);
        }

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