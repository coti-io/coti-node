package io.coti.dspnode.services;

import io.coti.common.communication.interfaces.ITransactionPropagationPublisher;
import io.coti.common.communication.interfaces.ITransactionSerializer;
import io.coti.common.data.TransactionData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.zeromq.ZMQ;

import javax.annotation.PostConstruct;

@Slf4j
@Service
public class ZeroMQTransactionPropagationPublisher implements ITransactionPropagationPublisher {
    private ZMQ.Context zeroMQContext;
    private ZMQ.Socket propagator;

    @Autowired
    private ITransactionSerializer transactionSerializer;

    @PostConstruct
    private void init() {
        zeroMQContext = ZMQ.context(1);
        propagator = zeroMQContext.socket(ZMQ.PUB);
        propagator.bind("tcp://localhost:8001");
    }

    @Override
    public void propagateTransaction(TransactionData transactionData) {
        log.info("Propagating transaction: {}", transactionData.getHash().toHexString());
        byte[] message = transactionSerializer.serializeTransaction(transactionData);
        propagator.sendMore("New Transactions".getBytes());
        propagator.send(message);
    }
}
