package io.coti.basenode.communication;

import io.coti.basenode.communication.data.ZeroMQMessageData;
import io.coti.basenode.communication.interfaces.IReceiver;
import io.coti.basenode.communication.interfaces.ISerializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import org.zeromq.ZMQException;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

@Slf4j
@Service
public class ZeroMQReceiver implements IReceiver {

    private HashMap<String, Consumer<Object>> classNameToHandlerMapping;
    private ZMQ.Context zeroMQContext;
    private ZMQ.Socket receiver;
    private BlockingQueue<ZeroMQMessageData> messageQueue;
    private Thread receiverThread;
    private Thread messagesQueueHandlerThread;
    @Autowired
    private ISerializer serializer;

    @Override
    public void init(String receivingPort, HashMap<String, Consumer<Object>> classNameToHandlerMapping) {
        this.classNameToHandlerMapping = classNameToHandlerMapping;
        zeroMQContext = ZMQ.context(1);
        receiver = zeroMQContext.socket(SocketType.ROUTER);
        receiver.bind("tcp://*:" + receivingPort);
        log.info("Zero MQ Client Connected!");
        messageQueue = new LinkedBlockingQueue<>();
    }

    @Override
    public void startListening() {
        receiverThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    String classType = receiver.recvStr();
                    if (classNameToHandlerMapping.containsKey(classType)) {
                        byte[] message = receiver.recv();
                        messageQueue.put(new ZeroMQMessageData(classType, message));
                    }
                } catch (InterruptedException e) {
                    log.info("ZMQ receiver interrupted");
                    Thread.currentThread().interrupt();
                } catch (ZMQException e) {
                    log.error("ZeroMQ exception at receiver thread", e);
                }
            }
            receiver.close();
        });
        receiverThread.start();
    }

    @Override
    public void initReceiverHandler() {
        messagesQueueHandlerThread = new Thread(this::handleMessagesQueueTask);
        messagesQueueHandlerThread.start();
    }

    private void handleMessagesQueueTask() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                ZeroMQMessageData zeroMQMessageData = messageQueue.take();
                Consumer<Object> consumer = classNameToHandlerMapping.get(zeroMQMessageData.getChannel());
                if (consumer != null) {
                    consumer.accept(serializer.deserialize(zeroMQMessageData.getMessage()));
                }
            } catch (InterruptedException e) {
                log.info("ZMQ receiver message handler interrupted");
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                log.error("ZMQ receiver message handler task error", e);
            }
        }
        LinkedList<ZeroMQMessageData> remainingMessages = new LinkedList<>();
        messageQueue.drainTo(remainingMessages);
        if (!remainingMessages.isEmpty()) {
            log.info("Please wait to process {} remaining messages", remainingMessages.size());
            remainingMessages.forEach(zeroMQMessageData -> {
                try {
                    Consumer<Object> consumer = classNameToHandlerMapping.get(zeroMQMessageData.getChannel());
                    if (consumer != null) {
                        consumer.accept(serializer.deserialize(zeroMQMessageData.getMessage()));
                    }
                } catch (Exception e) {
                    log.error("ZMQ receiver message handler task error", e);
                }
            });
        }
    }

    @Override
    public void shutdown() {
        try {
            if (receiver != null) {
                log.info("Shutting down {}", this.getClass().getSimpleName());
                zeroMQContext.term();
                receiverThread.interrupt();
                receiverThread.join();
                messagesQueueHandlerThread.interrupt();
                messagesQueueHandlerThread.join();
            }
        } catch (InterruptedException e) {
            log.error("Interrupted shutdown ZeroMQ subscriber");
            Thread.currentThread().interrupt();
        }
    }

}
