package io.coti.common.communication;

import io.coti.common.communication.interfaces.IReceiver;
import io.coti.common.communication.interfaces.ISerializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.zeromq.ZMQ;

import java.util.HashMap;
import java.util.function.Function;

@Slf4j
@Service
public class ZeroMQReceiver implements IReceiver {
    private HashMap<String, Function<Object, String>> classNameToHandlerMapping;

    @Value("${receiving.port}")
    private String receivingPort;

    private ZMQ.Context zeroMQContext;
    private ZMQ.Socket receiver;

    @Autowired
    private ISerializer serializer;

    @Override
    public void init(HashMap<String, Function<Object, String>> classNameToHandlerMapping) {
        this.classNameToHandlerMapping = classNameToHandlerMapping;
        zeroMQContext = ZMQ.context(1);
        receiver = zeroMQContext.socket(ZMQ.ROUTER);
        receiver.bind("tcp://*:" + receivingPort);
        log.info("Zero MQ Client Connected!");
        Thread receiverThread = new Thread(() -> {
            runReceiveLoop();
        });
        receiverThread.start();
    }

    private void runReceiveLoop() {
        while (true) {
            String classType = receiver.recvStr();
            if (classNameToHandlerMapping.containsKey(classType)) {
                log.info("Received a new Transaction...");
                byte[] message = receiver.recv();
                try {
                    String answer = classNameToHandlerMapping.get(classType).
                            apply(serializer.deserialize(message));
                } catch (ClassCastException e) {
                    log.error("Invalid request received: " + e.getMessage());
                }
            }
        }
    }
}
