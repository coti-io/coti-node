package io.coti.basenode.communication;

import io.coti.basenode.communication.data.MonitorSocketData;
import io.coti.basenode.communication.data.SenderSocketData;
import io.coti.basenode.communication.interfaces.ISender;
import io.coti.basenode.communication.interfaces.ISerializer;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.data.interfaces.IPropagatable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.zeromq.ZMQ;
import org.zeromq.ZMQException;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
public class ZeroMQSender implements ISender {

    private ZMQ.Context zeroMQContext;
    private Map<String, SenderSocketData> receivingAddressToSenderSocketMapping;
    @Autowired
    private ISerializer serializer;
    private boolean monitorInitialized;

    @PostConstruct
    private void init() {
        zeroMQContext = ZMQ.context(1);
        receivingAddressToSenderSocketMapping = new ConcurrentHashMap<>();
    }

    @Override
    public void connectToNode(String receivingServerAddress) {
        initializeSenderSocket(receivingServerAddress);
    }

    private void initializeSenderSocket(String addressAndPort) {
        SenderSocketData senderSocketData = receivingAddressToSenderSocketMapping.get(addressAndPort);
        if (senderSocketData == null) {
            senderSocketData = new SenderSocketData(zeroMQContext);
            ZMQ.Socket senderSocket = senderSocketData.getSenderSocket();
            ZMQ.Socket monitorSocket = senderSocketData.getMonitorSocketData().getMonitorSocket();
            senderSocketData.setMonitorThread(startMonitorThread(monitorSocket));
            if (senderSocket.connect(addressAndPort)) {
                log.info("ZeroMQ sender connected to address {}", addressAndPort);
                receivingAddressToSenderSocketMapping.put(addressAndPort, senderSocketData);
            } else {
                log.error("ZeroMQ sender failed to connect to address {}", addressAndPort);
            }
        } else {
            log.error("ZeroMQ sender already connected to address {}", addressAndPort);
        }
    }

    @Override
    public void initMonitor() {
        monitorInitialized = true;
    }

    private Thread startMonitorThread(ZMQ.Socket monitorSocket) {
        Thread monitorThread = new Thread(() -> {
            AtomicBoolean contextTerminated = new AtomicBoolean(false);
            while (!contextTerminated.get() && !Thread.currentThread().isInterrupted()) {
                try {
                    getEvent(monitorSocket, contextTerminated);
                } catch (ZMQException e) {
                    if (e.getErrorCode() == ZMQ.Error.ETERM.getCode()) {
                        contextTerminated.set(true);
                    } else if (e.getErrorCode() == ZMQ.Error.EINTR.getCode()) {
                        log.info("ZeroMQ subscriber thread is interrupted");
                        Thread.currentThread().interrupt();
                    } else {
                        log.error("ZeroMQ exception at monitor sender thread", e);
                    }
                } catch (Exception e) {
                    log.error("Exception at monitor sender thread", e);
                }
            }
           // monitorSocket.close();
        }, "MONITOR SENDER");
        monitorThread.start();
        return monitorThread;
    }

    private void getEvent(ZMQ.Socket monitorSocket, AtomicBoolean contextTerminated) {
        ZMQ.Event event = ZMQ.Event.recv(monitorSocket);
        if (event != null) {
            String address = event.getAddress();
            ZeroMQEvent zeroMQEvent = ZeroMQEvent.getEvent(event.getEvent());
            if (zeroMQEvent.isDisplayLog() && (zeroMQEvent.isDisplayBeforeInit() || monitorInitialized)) {
                log.info("ZeroMQ sender {} for address {}", zeroMQEvent, address);
            }
        } else {
            int errorCode = monitorSocket.base().errno();
            if (errorCode == ZMQ.Error.ETERM.getCode()) {
                contextTerminated.set(true);
            }
        }
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
            sender.disconnect(receivingFullAddress);
//            sender.unbind("tcp://*:" + senderSocketData.getSenderPort());

//            MonitorSocketData monitorSocketData = senderSocketData.getMonitorSocketData();
//            ZMQ.Socket monitorSocket = monitorSocketData.getMonitorSocket();
////            monitorSocket.unbind(monitorSocketData.getMonitorAddress());
//            monitorSocket.close();
//
//            Thread monitorThread = senderSocketData.getMonitorThread();
//            try {
//                monitorThread.interrupt();
//                monitorThread.join();
//            } catch (InterruptedException e) {
//                log.error("Interrupted ZeroMQ sender");
//                Thread.currentThread().interrupt();
//            }
            log.info("ZeroMQ sender closing connection with node of type {} and address {}", nodeType, receivingFullAddress);
            receivingAddressToSenderSocketMapping.remove(receivingFullAddress);
        } else {
            log.error("ZeroMQ sender doesn't have connection with node of type {} and address {}", nodeType, receivingFullAddress);
        }
    }
}
