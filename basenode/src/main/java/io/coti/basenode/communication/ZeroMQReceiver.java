package io.coti.basenode.communication;

import io.coti.basenode.communication.interfaces.IReceiver;
import io.coti.basenode.communication.interfaces.ISerializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.zeromq.SocketType;
import org.zeromq.ZMQ;

import java.util.HashMap;
import java.util.function.Consumer;

@Slf4j
@Service
public class ZeroMQReceiver implements IReceiver {

    private HashMap<String, Consumer<Object>> classNameToHandlerMapping;
    private ZMQ.Context zeroMQContext;
    private ZMQ.Socket receiver;
    @Autowired
    private ISerializer serializer;

    @Override
    public Thread init(String receivingPort, HashMap<String, Consumer<Object>> classNameToHandlerMapping) {
        this.classNameToHandlerMapping = classNameToHandlerMapping;
        zeroMQContext = ZMQ.context(1);
        receiver = zeroMQContext.socket(SocketType.ROUTER);
        receiver.bind("tcp://*:" + receivingPort);
        log.info("Zero MQ Client Connected!");
        Thread receiverThread = new Thread(this::runReceiveLoop);
        return receiverThread;
    }

    private void runReceiveLoop() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                String classType = receiver.recvStr();
                if (classNameToHandlerMapping.containsKey(classType)) {
                    log.debug("Received a new {}", classType);
                    byte[] message = receiver.recv();

                    classNameToHandlerMapping.get(classType).
                            accept(serializer.deserialize(message));

                }
            } catch (Exception e) {
                log.error("ZeroMQ receiver error. {}: {} ", e.getClass().getName(), e.getMessage());
            }
        }
    }
}
