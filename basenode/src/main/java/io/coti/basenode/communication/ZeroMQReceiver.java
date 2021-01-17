package io.coti.basenode.communication;

import io.coti.basenode.communication.data.ZeroMQMessageData;
import io.coti.basenode.communication.interfaces.IReceiver;
import io.coti.basenode.communication.interfaces.ISerializer;
import io.coti.basenode.data.interfaces.IPropagatable;
import io.coti.basenode.exceptions.ZeroMQReceiverException;
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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

@Slf4j
@Service
public class ZeroMQReceiver implements IReceiver {

    private HashMap<String, Consumer<IPropagatable>> classNameToHandlerMapping;
    private ZMQ.Context zeroMQContext;
    private SocketType socketType;
    private ZMQ.Socket receiver;
    private ZMQ.Socket monitorSocket;
    private BlockingQueue<ZeroMQMessageData> messageQueue;
    private Thread receiverThread;
    private Thread monitorThread;
    private Thread messagesQueueHandlerThread;
    @Autowired
    private ISerializer serializer;
    private final AtomicBoolean monitorInitialized = new AtomicBoolean(false);

    @Override
    public void init(String receivingPort, HashMap<String, Consumer<IPropagatable>> classNameToHandlerMapping) {
        this.classNameToHandlerMapping = classNameToHandlerMapping;
        zeroMQContext = ZMQ.context(1);
        socketType = SocketType.ROUTER;
        receiver = zeroMQContext.socket(socketType);
        monitorSocket = ZeroMQUtils.createAndConnectMonitorSocket(zeroMQContext, receiver);
        if (!receiver.bind("tcp://*:" + receivingPort)) {
            throw new ZeroMQReceiverException("ZeroMQ receiver socket bind failed to receiver port " + receivingPort);
        }
        log.info("Zero MQ Client Connected!");
        messageQueue = new LinkedBlockingQueue<>();
    }

    @Override
    public void initMonitor() {
        monitorInitialized.set(true);
    }

    @Override
    public void startListening() {
        startReceiverThread();
        startMonitorThread();
    }

    private void startReceiverThread() {
        receiverThread = new Thread(() -> {
            boolean contextTerminated = false;
            while (!contextTerminated && !Thread.currentThread().isInterrupted()) {
                try {
                    String classType = receiver.recvStr();
                    addToMessageQueue(classType);
                } catch (ZMQException e) {
                    if (e.getErrorCode() == ZMQ.Error.ETERM.getCode()) {
                        log.info("ZeroMQ receiver context terminated");
                        contextTerminated = true;
                    } else {
                        log.error("ZeroMQ exception at receiver thread", e);
                    }
                } catch (Exception e) {
                    log.error("Error at receiver thread", e);
                }
            }
            receiver.close();
        }, "ROUTER");
        receiverThread.start();
    }

    private void startMonitorThread() {
        monitorThread = new Thread(() -> {
            AtomicBoolean contextTerminated = new AtomicBoolean(false);
            while (!contextTerminated.get() && !Thread.currentThread().isInterrupted()) {
                try {
                    ZeroMQUtils.getServerSocketEvent(monitorSocket, socketType, monitorInitialized, contextTerminated);
                } catch (ZMQException e) {
                    if (e.getErrorCode() == ZMQ.Error.ETERM.getCode()) {
                        log.info("ZeroMQ receiver context terminated");
                        contextTerminated.set(true);
                    } else {
                        log.error("ZeroMQ exception at monitor receiver thread", e);
                    }
                } catch (Exception e) {
                    log.error("Exception at monitor receiver thread", e);
                }
            }
            monitorSocket.close();
        }, "MONITOR ROUTER");
        monitorThread.start();
    }

    private void addToMessageQueue(String classType) {
        try {
            if (classNameToHandlerMapping.containsKey(classType)) {
                byte[] message = receiver.recv();
                messageQueue.put(new ZeroMQMessageData(classType, message));
            }
        } catch (InterruptedException e) {
            log.info("ZMQ receiver interrupted");
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void initReceiverHandler() {
        messagesQueueHandlerThread = new Thread(this::handleMessagesQueueTask, "ROUTER HANDLER");
        messagesQueueHandlerThread.start();
    }

    private void handleMessagesQueueTask() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                ZeroMQMessageData zeroMQMessageData = messageQueue.take();
                Consumer<IPropagatable> consumer = classNameToHandlerMapping.get(zeroMQMessageData.getChannel());
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
                    Consumer<IPropagatable> consumer = classNameToHandlerMapping.get(zeroMQMessageData.getChannel());
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
                monitorThread.interrupt();
                monitorThread.join();
            }
        } catch (InterruptedException e) {
            log.error("Interrupted shutdown ZeroMQ receiver");
            Thread.currentThread().interrupt();
        }
    }

}
