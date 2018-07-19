package io.coti.common.communication;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.zeromq.ZMQ;

import java.util.function.Consumer;

@Slf4j
@Service
public class MultiClientZeroMQSubscriber {
    ZMQ.Context zeroMQContext;
    ZMQ.Socket receiver;

    public void subscribe(String senderAddress, Consumer<byte[]> handler) {
        zeroMQContext = ZMQ.context(1);
        receiver = zeroMQContext.socket(ZMQ.SUB);
        receiver.bind("tcp://localhost:8003");
        receiver.connect(senderAddress);
        receiver.subscribe("New Transactions".getBytes());

        ZMQ.Socket sender = zeroMQContext.socket(ZMQ.REQ);
        sender.bind("tcp://localhost:8005");
        sender.connect("tcp://localhost:8006");
        sender.send("1");
        log.info("Zero MQ Client connected to: {}", senderAddress);

        Thread receiverThread = new Thread(() -> {
            while (!Thread.interrupted()) {
                String channel = receiver.recvStr();
                log.info("Received a new message on channel: {}", channel);
                byte[] message = receiver.recv();
                handler.accept(message);
            }
        });
        receiverThread.start();
    }
}
