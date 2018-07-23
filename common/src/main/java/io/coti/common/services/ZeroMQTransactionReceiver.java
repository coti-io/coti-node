package io.coti.common.services;

import io.coti.common.communication.interfaces.ITransactionReceiver;
import io.coti.common.communication.interfaces.ITransactionSerializer;
import io.coti.common.data.TransactionData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.zeromq.ZMQ;

import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
@Service
public class ZeroMQTransactionReceiver implements ITransactionReceiver {
    @Value("${transaction.receiving.port}")
    private String transactionReceivingPort;

    private ZMQ.Context zeroMQContext;
    private ZMQ.Socket receiver;

    @Autowired
    private ITransactionSerializer transactionSerializer;

    @Override
    public void init(Function<TransactionData, String> unconfirmedTransactionsHandler) {
        System.out.println("Transaction sender starting...");
        zeroMQContext = ZMQ.context(1);
        receiver = zeroMQContext.socket(ZMQ.REP);
        receiver.bind("tcp://*:" + transactionReceivingPort);
        System.out.println("Zero MQ Client Connected!");
        Thread receiverThread = new Thread(() -> {
            runReceiveLoop(unconfirmedTransactionsHandler);
        });
        receiverThread.start();
    }

    private void runReceiveLoop(Function<TransactionData, String> unconfirmedTransactionsHandler) {
        while (true) {
            byte[] message = receiver.recv();
            log.info("Received a new message...");
            String answer = unconfirmedTransactionsHandler.apply(transactionSerializer.deserializeMessage(message));
            receiver.send(answer);
        }
    }
}
