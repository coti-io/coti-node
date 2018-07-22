package io.coti.dspnode.services;

import io.coti.common.communication.interfaces.ITransactionReceiver;
import io.coti.common.communication.interfaces.ITransactionSerializer;
import io.coti.common.data.TransactionData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.zeromq.ZMQ;

import java.util.function.Consumer;

@Slf4j
@Service
public class ZeroMQTransactionReceiver implements ITransactionReceiver {
    ZMQ.Context zeroMQContext;
    ZMQ.Socket receiver;

    @Autowired
    private ITransactionSerializer transactionSerializer;

    @Override
    public void init(Consumer<TransactionData> unconfirmedTransactionsHandler) {
        System.out.println("Transaction sender starting...");
        zeroMQContext = ZMQ.context(1);
        receiver = zeroMQContext.socket(ZMQ.REP);
        receiver.bind("tcp://localhost:8002");
        System.out.println("Zero MQ Client Connected!");
        Thread receiverThread = new Thread(() -> {
            runReceiveLoop(unconfirmedTransactionsHandler);
        });
        receiverThread.start();
    }

    private void runReceiveLoop(Consumer<TransactionData> unconfirmedTransactionsHandler) {
        while (true) {
            byte[] message = receiver.recv();
            log.info("Received a new message...");
            unconfirmedTransactionsHandler.accept(transactionSerializer.deserializeMessage(message));
            receiver.send("Received a message...");
        }
    }
}
