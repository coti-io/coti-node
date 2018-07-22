package io.coti.fullnode.services;

import io.coti.common.communication.ZeroMQUtils;
import io.coti.common.communication.interfaces.ITransactionSender;
import io.coti.common.communication.interfaces.ITransactionSerializer;
import io.coti.common.data.TransactionData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.zeromq.ZMQ;

import javax.annotation.PostConstruct;

@Slf4j
@Service
public class ZeroMQTransactionSender implements ITransactionSender {
    private ZMQ.Context zeroMQContext;
    private ZMQ.Socket sender;

    @Autowired
    private ITransactionSerializer transactionSerializer;

    @PostConstruct
    private void init() {
        zeroMQContext = ZMQ.context(1);
        sender = zeroMQContext.socket(ZMQ.REQ);
        ZeroMQUtils.bindToRandomPort(sender);
        sender.connect("tcp://localhost:8002");
    }

    @Override
    public void sendTransaction(TransactionData transactionData) {
        byte[] message = transactionSerializer.serializeTransaction(transactionData);
        sender.send(message);
    }
}
