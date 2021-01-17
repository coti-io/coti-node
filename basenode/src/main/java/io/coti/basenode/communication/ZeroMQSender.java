package io.coti.basenode.communication;

import io.coti.basenode.communication.data.MonitorSocketData;
import io.coti.basenode.communication.data.ReconnectMonitorData;
import io.coti.basenode.communication.data.SenderSocketData;
import io.coti.basenode.communication.interfaces.ISender;
import io.coti.basenode.communication.interfaces.ISerializer;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.data.interfaces.IPropagatable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import org.zeromq.ZMQException;

import javax.annotation.PostConstruct;
import java.nio.channels.ClosedSelectorException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
public class ZeroMQSender implements ISender {

    private ZMQ.Context zeroMQContext;
    private SocketType socketType;
    private Map<String, SenderSocketData> receivingAddressToSenderSocketMapping;
    @Autowired
    private ISerializer serializer;
    private final AtomicBoolean monitorInitialized = new AtomicBoolean(false);
    private Thread monitorReconnectThread;
    private final Map<String, ReconnectMonitorData> addressToReconnectMonitorMap = new ConcurrentHashMap<>();

    @PostConstruct
    private void init() {
        zeroMQContext = ZMQ.context(1);
        socketType = SocketType.DEALER;
        receivingAddressToSenderSocketMapping = new ConcurrentHashMap<>();
    }

    @Override
    public void connectToNode(String receivingServerAddress, NodeType nodeType) {
        SenderSocketData senderSocketData = receivingAddressToSenderSocketMapping.get(receivingServerAddress);
        if (senderSocketData == null) {
            senderSocketData = new SenderSocketData(zeroMQContext, nodeType);
            ZMQ.Socket senderSocket = senderSocketData.getSenderSocket();
            ZMQ.Socket monitorSocket = senderSocketData.getMonitorSocketData().getMonitorSocket();
            senderSocketData.setMonitorThread(startMonitorThread(monitorSocket, receivingServerAddress));
            if (senderSocket.connect(receivingServerAddress)) {
                log.info("ZeroMQ sender connected to address {}", receivingServerAddress);
                receivingAddressToSenderSocketMapping.put(receivingServerAddress, senderSocketData);
            } else {
                log.error("ZeroMQ sender failed to connect to address {}", receivingServerAddress);
            }
        } else {
            log.error("ZeroMQ sender already connected to address {}", receivingServerAddress);
        }
    }

    @Override
    public void initMonitor() {
        monitorInitialized.set(true);
    }

    private Thread startMonitorThread(ZMQ.Socket monitorSocket, String receiverAddress) {
        Thread monitorThread = new Thread(() -> {
            AtomicBoolean contextTerminated = new AtomicBoolean(false);
            AtomicBoolean closedSelector = new AtomicBoolean(false);
            while (!contextTerminated.get() && !closedSelector.get() && !Thread.currentThread().isInterrupted()) {
                getEvent(monitorSocket, contextTerminated, closedSelector);
            }
            ZeroMQUtils.closeSocket(monitorSocket);
            log.info("ZeroMQ sender monitor thread is ending for receiver {}.", receiverAddress);
        }, "MONITOR DEALER");
        monitorThread.start();
        return monitorThread;
    }

    private void getEvent(ZMQ.Socket monitorSocket, AtomicBoolean contextTerminated, AtomicBoolean closedSelector) {
        try {
            getEvent(monitorSocket, contextTerminated);
        } catch (ZMQException e) {
            if (e.getErrorCode() == ZMQ.Error.ETERM.getCode()) {
                contextTerminated.set(true);
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

    private void getEvent(ZMQ.Socket monitorSocket, AtomicBoolean contextTerminated) {
        if (monitorReconnectThread == null) {
            monitorReconnectThread = ZeroMQUtils.getMonitorReconnectThread(addressToReconnectMonitorMap, socketType);
        }
        ZeroMQUtils.getClientServerEvent(monitorSocket, socketType, monitorInitialized, contextTerminated, addressToReconnectMonitorMap, this::getNodeTypeByAddress);
    }

    private NodeType getNodeTypeByAddress(String address) {
        return Optional.ofNullable(receivingAddressToSenderSocketMapping.get(address)).map(SenderSocketData::getNodeType).orElse(null);
    }

    @Override
    public <T extends IPropagatable> void send(T toSend, String address) {
        byte[] message = serializer.serialize(toSend);
        synchronized (this) {
            try {
                ZMQ.Socket senderSocket = receivingAddressToSenderSocketMapping.get(address).getSenderSocket();
                senderSocket.sendMore(toSend.getClass().getName());
                senderSocket.send(message);
                log.debug("Message {} was sent to {}", toSend, address);
            } catch (ZMQException exception) {
                log.error("Exception in sending", exception);
            }
        }
    }

    @Override
    public void disconnectFromNode(String receivingFullAddress, NodeType nodeType) {
        SenderSocketData senderSocketData = receivingAddressToSenderSocketMapping.get(receivingFullAddress);
        if (senderSocketData != null) {
            ZMQ.Socket sender = senderSocketData.getSenderSocket();
            ZeroMQUtils.closeSocket(sender);
            try {
                // Waiting to sender socket to close
                Thread.sleep(100);
            } catch (InterruptedException e) {
                log.error("Interrupted {}", Thread.currentThread().getName());
                Thread.currentThread().interrupt();
            }

            MonitorSocketData monitorSocketData = senderSocketData.getMonitorSocketData();
            ZMQ.Socket monitorSocket = monitorSocketData.getMonitorSocket();
            ZeroMQUtils.closeSocket(monitorSocket);

            Thread monitorThread = senderSocketData.getMonitorThread();
            try {
                monitorThread.interrupt();
                monitorThread.join();
            } catch (InterruptedException e) {
                log.error("Interrupted {}", Thread.currentThread().getName());
                Thread.currentThread().interrupt();
            }
            log.info("ZeroMQ sender closing connection with node of type {} and address {}", nodeType, receivingFullAddress);
            receivingAddressToSenderSocketMapping.remove(receivingFullAddress);
            ZeroMQUtils.removeFromReconnectMonitor(addressToReconnectMonitorMap, receivingFullAddress);
        } else {
            log.error("ZeroMQ sender doesn't have connection with node of type {} and address {}", nodeType, receivingFullAddress);
        }
    }
}
