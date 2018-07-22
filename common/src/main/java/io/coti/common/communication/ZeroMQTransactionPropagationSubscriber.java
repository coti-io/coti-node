package io.coti.common.communication;

import io.coti.common.communication.interfaces.ITransactionPropagationSubscriber;
import io.coti.common.communication.interfaces.ITransactionSerializer;
import io.coti.common.data.TransactionData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.zeromq.ZMQ;

import java.util.List;
import java.util.function.Consumer;

@Slf4j
@Service
public class ZeroMQTransactionPropagationSubscriber implements ITransactionPropagationSubscriber {
    @Value("#{'${server.addresses}'.split(',')}")
    private List<String> serverAddresses;

    private ZMQ.Context zeroMQContext;
    private ZMQ.Socket propagationReceiver;

    @Autowired
    private ITransactionSerializer transactionSerializer;


    public void init(Consumer<TransactionData> unconfirmedTransactionsHandler) {
        zeroMQContext = ZMQ.context(1);
        initSockets();

        Thread receiverThread = new Thread(() -> {
            while (!Thread.interrupted()) {
                String channel = propagationReceiver.recvStr();
                log.info("Received a new message on channel: {}", channel);
                byte[] message = propagationReceiver.recv();
                TransactionData transactionData = null;
                transactionData = transactionSerializer.deserializeMessage(message);
                unconfirmedTransactionsHandler.accept(transactionData);
            }
        });
        receiverThread.start();
    }

    private void initSockets() {
        propagationReceiver = zeroMQContext.socket(ZMQ.SUB);
        ZeroMQUtils.bindToRandomPort(propagationReceiver);
        for (String serverAddress :
                serverAddresses
                ) {
            propagationReceiver.connect(serverAddress);
            propagationReceiver.subscribe("New Transactions".getBytes());
        }
    }
}