package io.coti.common.services;

import io.coti.common.communication.ZeroMQUtils;
import io.coti.common.communication.interfaces.ITransactionSender;
import io.coti.common.communication.interfaces.ITransactionSerializer;
import io.coti.common.data.TransactionData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.zeromq.ZMQ;
import org.zeromq.ZMQException;

import javax.annotation.PostConstruct;
import java.util.List;

@Slf4j
@Service
public class ZeroMQTransactionSender implements ITransactionSender {
    @Value("#{'${receiving.server.addresses}'.split(',')}")
    private List<String> receivingServerAddresses;
    private ZMQ.Context zeroMQContext;
    private ZMQ.Socket sender;

    @Autowired
    private ITransactionSerializer transactionSerializer;

    @PostConstruct
    private void init() {
        zeroMQContext = ZMQ.context(1);
        sender = zeroMQContext.socket(ZMQ.REQ);
        ZeroMQUtils.bindToRandomPort(sender);
        for (String receivingServerAddress:
                receivingServerAddresses
             ) {
            sender.connect(receivingServerAddress);
        }
    }

    @Override
    public void sendTransaction(TransactionData transactionData) {
        byte[] message = transactionSerializer.serializeTransaction(transactionData);
        try {
            sender.send(message);
        } catch (ZMQException exception) {
            return;
        }
    }
}
