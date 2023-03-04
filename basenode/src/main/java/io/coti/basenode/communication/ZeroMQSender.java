package io.coti.basenode.communication;

import io.coti.basenode.communication.data.ReconnectMonitorData;
import io.coti.basenode.communication.data.SenderSocketData;
import io.coti.basenode.communication.data.ZeroMQMessageData;
import io.coti.basenode.communication.interfaces.ISender;
import io.coti.basenode.data.ConnectionZMQData;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.data.interfaces.IPropagatable;
import io.coti.basenode.exceptions.CotiRunTimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import org.zeromq.ZMQException;

import javax.annotation.PostConstruct;
import java.nio.channels.ClosedSelectorException;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.coti.basenode.services.BaseNodeServiceManager.serializer;

@Slf4j
@Service
public class ZeroMQSender implements ISender {

    private static final String INTERRUPTED_MESSAGE = "ZeroMQSender interrupted ";
    private static final String CONTEXT_TERMINATED = "CONTEXT_TERMINATED";
    private ZMQ.Context zeroMQContext;
    private SocketType socketType;
    private ZMQ.Socket sender;
    private ZMQ.Socket monitorSocket;
    private String monitorAddress;
    @Value("${server.ip}")
    private String serverIp;
    @Value("${application.name}")
    private String applicationName;
    private final CountDownLatch countDownLatch = new CountDownLatch(1);
    private Thread senderThread;
    private Thread monitorThread;
    private Map<String, SenderSocketData> receivingAddressToSenderSocketMapping;
    private BlockingQueue<ZeroMQMessageData> sendMessageQueue;
    private final AtomicBoolean monitorInitialized = new AtomicBoolean(false);
    private Thread monitorReconnectThread;
    private final Map<String, ReconnectMonitorData> addressToReconnectMonitorMap = new ConcurrentHashMap<>();

    @PostConstruct
    private void init() {
        zeroMQContext = ZeroMQContext.getZeroMQContext();
        socketType = SocketType.DEALER;
        sendMessageQueue = new LinkedBlockingQueue<>();
        receivingAddressToSenderSocketMapping = new ConcurrentHashMap<>();
        startSenderThread();
        startMonitorThread();
    }

    private void startSenderThread() {
        senderThread = new Thread(() -> {
            sender = zeroMQContext.socket(socketType);
            String identity = String.format("%s:%s::%d", applicationName, serverIp, Instant.now().toEpochMilli());
            sender.setIdentity(identity.getBytes());
            sender.setHWM(10000);
            sender.setLinger(100);
            try {
                monitorAddress = ZeroMQUtils.createAndStartMonitorOnSocket(sender);
                countDownLatch.countDown();
                startSendingMessages();
            } catch (CotiRunTimeException e) {
                log.error("ZeroMQSender runtime exception : ", e);
            } finally {
                ZeroMQUtils.closeSocket(sender);
            }
        }, "DEALER");
        senderThread.start();
    }

    private void startSendingMessages() {
        while (!ZeroMQContext.isContextTerminated() && !Thread.currentThread().isInterrupted()) {
            try {
                ZeroMQMessageData messageData = sendMessageQueue.take();
                processMessage(messageData);
            } catch (InterruptedException e) {
                log.error(INTERRUPTED_MESSAGE, e);
                Thread.currentThread().interrupt();
            } catch (ZMQException e) {
                if (e.getErrorCode() == ZMQ.Error.ETERM.getCode()) {
                    log.info("ZMQ Context is terminated");
                    ZeroMQContext.setContextTerminated(true);
                } else if (e.getErrorCode() == ZMQ.Error.EINTR.getCode()) {
                    log.info("ZeroMQ sender thread is interrupted");
                    break;
                } else {
                    log.error("ZeroMQ exception at sender thread", e);
                }
            }
        }
    }

    private void processMessage(ZeroMQMessageData messageData) {
        if (ConnectionZMQData.class.getName().equals(messageData.getChannel())) {
            connectSender(messageData);
        } else if (CONTEXT_TERMINATED.equals(messageData.getChannel())) {
            log.info("ZeroMQ sender interrupted in thread ID: {} of thread: {}", Thread.currentThread().getId(), Thread.currentThread().getName());
        } else {
            sendMessage(messageData);
        }
    }

    private void connectSender(ZeroMQMessageData messageData) {
        byte[] message = messageData.getMessage();
        ConnectionZMQData connectionData = (ConnectionZMQData) serializer.deserialize(message);
        String receivingServerAddress = connectionData.getConnectionAddress();
        if (sender.connect(receivingServerAddress)) {
            log.info("ZeroMQ sender connecting in thread ID: {} of thread: {}", Thread.currentThread().getId(), Thread.currentThread().getName());
            log.info("ZeroMQ sender connected to address {}", receivingServerAddress);
            String[] portArray = connectionData.getConnectionAddress().split(":");
            SenderSocketData senderSocketData = new SenderSocketData(Integer.parseInt(portArray[2]), connectionData.getConnectionNodeType());
            receivingAddressToSenderSocketMapping.put(receivingServerAddress, senderSocketData);
        } else {
            log.error("ZeroMQ sender failed to connect to address {}", receivingServerAddress);
        }
    }


    @Override
    public void connectToNode(String receivingServerAddress, NodeType nodeType) {
        SenderSocketData senderSocketData = receivingAddressToSenderSocketMapping.get(receivingServerAddress);
        if (senderSocketData == null) {
            try {
                sendMessageQueue.put(new ZeroMQMessageData(ConnectionZMQData.class.getName(), serializer.serialize(new ConnectionZMQData(receivingServerAddress, nodeType))));
            } catch (InterruptedException e) {
                log.error(INTERRUPTED_MESSAGE, e);
                Thread.currentThread().interrupt();
            }
        } else {
            log.error("ZeroMQ sender already connected to address {}", receivingServerAddress);
        }
    }

    @Override
    public void initMonitor() {
        monitorInitialized.set(true);
    }

    private void startMonitorThread() {
        monitorThread = new Thread(() -> {
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            monitorSocket = ZeroMQUtils.createAndConnectMonitorSocket(zeroMQContext, monitorAddress);
            AtomicBoolean closedSelector = new AtomicBoolean(false);
            while (!ZeroMQContext.isContextTerminated() && !closedSelector.get() && !Thread.currentThread().isInterrupted()) {
                getEvent(monitorSocket, closedSelector);
            }
            try {
                if (ZeroMQContext.isContextTerminated()) {
                    sendMessageQueue.put(new ZeroMQMessageData(CONTEXT_TERMINATED, null));
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                ZeroMQUtils.closeSocket(monitorSocket);
                log.info("ZeroMQ sender monitor thread is ending, thread ID {}.", Thread.currentThread().getId());
            }
        }, "MONITOR DEALER");
        monitorThread.start();
    }

    private void getEvent(ZMQ.Socket monitorSocket, AtomicBoolean closedSelector) {
        try {
            getEvent(monitorSocket);
        } catch (ZMQException e) {
            if (e.getErrorCode() == ZMQ.Error.ETERM.getCode()) {
                ZeroMQContext.setContextTerminated(true);
            } else {
                log.error("ZeroMQ exception at monitor sender thread", e);
            }
        } catch (ClosedSelectorException e) {
            log.info("ZeroMQ monitor sender thread has closed selector");
            closedSelector.set(true);
        } catch (Exception e) {
            log.error("Exception at monitor sender thread", e);
        }
    }

    private void getEvent(ZMQ.Socket monitorSocket) {
        if (monitorReconnectThread == null) {
            monitorReconnectThread = ZeroMQUtils.getMonitorReconnectThread(addressToReconnectMonitorMap, socketType);
        }
        ZeroMQUtils.getClientServerEvent(monitorSocket, socketType, monitorInitialized, addressToReconnectMonitorMap, this::getNodeTypeByAddress);
    }

    private NodeType getNodeTypeByAddress(String address) {
        return Optional.ofNullable(receivingAddressToSenderSocketMapping.get(address)).map(SenderSocketData::getNodeType).orElse(null);
    }

    @Override
    public <T extends IPropagatable> void send(T toSend, String address) {
        byte[] message = serializer.serialize(toSend);
        try {
            sendMessageQueue.put(new ZeroMQMessageData(toSend.getClass().getName(), message));
        } catch (InterruptedException e) {
            log.error(INTERRUPTED_MESSAGE, e);
            Thread.currentThread().interrupt();
        }
    }

    private void sendMessage(ZeroMQMessageData messageData) {
        sender.sendMore(messageData.getChannel().getBytes());
        boolean isSent = sender.send(messageData.getMessage());
        log.info("ZeroMQ sender sending msg in thread ID: {} of thread: {}", Thread.currentThread().getId(), Thread.currentThread().getName());
        if (!isSent) {
            log.error("Error {} at sender socket. Socket channel status: {}", sender.errno(), sender.getFD().isOpen());
            return;
        }
        log.debug("Message {} was sent to ROUTERS", messageData.getChannel());
    }

    @Override
    public void disconnectFromNode(String receivingFullAddress, NodeType nodeType) {
        SenderSocketData senderSocketData = receivingAddressToSenderSocketMapping.get(receivingFullAddress);
        if (senderSocketData != null) {
            sender.disconnect(receivingFullAddress);
            log.info("ZeroMQ sender closing connection with node of type {} and address {}", nodeType, receivingFullAddress);
            receivingAddressToSenderSocketMapping.remove(receivingFullAddress);
            ZeroMQUtils.removeFromReconnectMonitor(addressToReconnectMonitorMap, receivingFullAddress);
        } else {
            log.error("ZeroMQ sender doesn't have connection with node of type {} and address {}", nodeType, receivingFullAddress);
        }
    }

    @Override
    public void shutdown() {
        if (sender != null) {
            log.info("Shutting down {}", this.getClass().getSimpleName());
            try {
                senderThread.interrupt();
                senderThread.join();
                monitorThread.interrupt();
                monitorThread.join();
                if (monitorReconnectThread != null) {
                    monitorReconnectThread.interrupt();
                    monitorReconnectThread.join();
                }
            } catch (InterruptedException e) {
                log.error(INTERRUPTED_MESSAGE, e);
                Thread.currentThread().interrupt();
            }
        }
    }
}
