package io.coti.common.services;

import io.coti.common.communication.interfaces.ISerializer;
import io.coti.common.communication.interfaces.publisher.IPropagationPublisher;
import io.coti.common.data.AddressData;
import io.coti.common.data.TransactionData;
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
    private String transactionPropagationPort;
    private ZMQ.Context zeroMQContext;
    private ZMQ.Socket propagator;

    @Autowired
    private ISerializer transactionSerializer;

    @PostConstruct
    private void init() {
        zeroMQContext = ZMQ.context(1);
        propagator = zeroMQContext.socket(ZMQ.PUB);
        propagator.bind("tcp://*:" + transactionPropagationPort);
    }

    @Override
    public void propagateTransaction(TransactionData transactionData, String channel) {
        log.info("Propagating transaction {} to {}", transactionData.getHash().toHexString(), channel);
        byte[] message = transactionSerializer.serializeTransaction(transactionData);
        propagator.sendMore(channel.getBytes());
        propagator.send(message);
    }

    @Override
    public void propagateAddress(AddressData addressData, String channel) {
        log.info("Propagating address {} to {}", addressData.getHash().toHexString(), channel);
        byte[] message = transactionSerializer.serializeAddress(addressData);
        propagator.sendMore(channel.getBytes());
        propagator.send(message);
    }
}