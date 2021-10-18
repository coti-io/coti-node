package io.coti.basenode.communication;

import io.coti.basenode.communication.data.MonitorSocketData;
import io.coti.basenode.communication.data.ReconnectMonitorData;
import io.coti.basenode.data.NodeType;
import lombok.extern.slf4j.Slf4j;
import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import org.zeromq.ZMQException;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

@Slf4j
public class ZeroMQUtils {

    private ZeroMQUtils() {
    }

    public static int bindToRandomPort(ZMQ.Socket socket) {
        boolean success = false;
        int port = 10000;
        while (!success) {
            try {
                success = socket.bind("tcp://*:" + port);
                if (!success) {
                    port++;
                }
            } catch (ZMQException exception) {
                port++;
            }
        }
        return port;
    }

    public static void closeSocket(ZMQ.Socket socket) {
        if (!Thread.currentThread().isInterrupted()) {
            socket.close();
        }
    }

    public static ZMQ.Socket createAndConnectMonitorSocket(ZMQ.Context zeroMQContext, ZMQ.Socket socket) {
        String monitorAddress = getMonitorSocketAddress(socket);
        return createAndConnectMonitorSocket(zeroMQContext, socket, monitorAddress);
    }

    private static ZMQ.Socket createAndConnectMonitorSocket(ZMQ.Context zeroMQContext, ZMQ.Socket socket, String monitorAddress) {
        socket.monitor(monitorAddress, ZMQ.EVENT_ALL);
        ZMQ.Socket monitorSocket = zeroMQContext.socket(SocketType.PAIR);
        monitorSocket.setLinger(100);
        monitorSocket.connect(monitorAddress);
        return monitorSocket;
    }

    private static String getMonitorSocketAddress(ZMQ.Socket socket) {
        return "inproc://" + socket.getSocketType().name() + Instant.now().toEpochMilli();
    }

    public static MonitorSocketData getMonitorSocketData(ZMQ.Context zeroMQContext, ZMQ.Socket socket) {
        String monitorAddress = getMonitorSocketAddress(socket);
        ZMQ.Socket monitorSocket = createAndConnectMonitorSocket(zeroMQContext, socket, monitorAddress);
        return new MonitorSocketData(monitorSocket, monitorAddress);
    }

    public static void getServerSocketEvent(ZMQ.Socket monitorSocket, SocketType socketType, AtomicBoolean monitorInitialized, AtomicBoolean contextTerminated) {
        ZMQ.Event event = ZMQ.Event.recv(monitorSocket);
        if (event != null) {
            ZeroMQEvent zeroMQEvent = ZeroMQEvent.getEvent(event.getEvent());
            if (zeroMQEvent.isDisplayLog() && (zeroMQEvent.isDisplayBeforeInit() || monitorInitialized.get())) {
                log.info("ZeroMQ {} event: {}", socketType, zeroMQEvent);
            }

        } else {
            nullEventHandler(monitorSocket, socketType, contextTerminated);
        }
    }

    private static void nullEventHandler(ZMQ.Socket monitorSocket, SocketType socketType, AtomicBoolean contextTerminated) {
        int errorCode = monitorSocket.base().errno();
        if (errorCode == ZMQ.Error.ETERM.getCode()) {
            log.info("ZeroMQ {} context terminated", socketType);
            contextTerminated.set(true);
        }
    }

    public static void getClientServerEvent(ZMQ.Socket monitorSocket, SocketType socketType, AtomicBoolean monitorInitialized, AtomicBoolean contextTerminated,
                                            Map<String, ReconnectMonitorData> addressToReconnectMonitorMap, Function<String, NodeType> getNodeTypeByAddress) {
        ZMQ.Event event = ZMQ.Event.recv(monitorSocket);
        if (event != null) {
            String address = event.getAddress();
            ZeroMQEvent zeroMQEvent = ZeroMQEvent.getEvent(event.getEvent());
            if (zeroMQEvent.isDisplayLog() && (zeroMQEvent.isDisplayBeforeInit() || monitorInitialized.get())) {
                log.info("ZeroMQ {} event {} for address {}", socketType, zeroMQEvent, address);
            }
            if (zeroMQEvent.equals(ZeroMQEvent.DISCONNECTED)) {
                NodeType nodeType = getNodeTypeByAddress.apply(address);
                addToReconnectMonitor(addressToReconnectMonitorMap, address, nodeType);
            } else if (zeroMQEvent.equals(ZeroMQEvent.CONNECTED)) {
                removeFromReconnectMonitor(addressToReconnectMonitorMap, address);
            } else if (zeroMQEvent.equals(ZeroMQEvent.CONNECT_RETRIED)) {
                incrementRetriesInReconnectMonitor(addressToReconnectMonitorMap, address);
            }
        } else {
            nullEventHandler(monitorSocket, socketType, contextTerminated);
        }
    }

    public static void addToReconnectMonitor(Map<String, ReconnectMonitorData> addressToReconnectMonitorMap, String address, NodeType nodeType) {
        Optional<ReconnectMonitorData> optionalPutReconnectMonitorData = Optional.ofNullable(addressToReconnectMonitorMap.putIfAbsent(address, new ReconnectMonitorData(nodeType)));
        if (!optionalPutReconnectMonitorData.isPresent()) {
            log.info("Reconnect monitor is started for node {} and type {}", address, nodeType);
        }
    }

    public static void incrementRetriesInReconnectMonitor(Map<String, ReconnectMonitorData> addressToReconnectMonitorMap, String address) {
        Optional.ofNullable(addressToReconnectMonitorMap.get(address)).ifPresent(reconnectMonitorData ->
                reconnectMonitorData.getRetriesNumber().incrementAndGet()
        );
    }

    public static void removeFromReconnectMonitor(Map<String, ReconnectMonitorData> addressToReconnectMonitorMap, String address) {
        Optional.ofNullable(addressToReconnectMonitorMap.remove(address)).ifPresent(reconnectMonitorData ->
                log.info("Reconnect monitor is finished for node {} and type {}", address, reconnectMonitorData.getNodeType())
        );
    }

    public static Thread getMonitorReconnectThread(Map<String, ReconnectMonitorData> addressToReconnectMonitorMap, SocketType socketType) {
        Thread monitorReconnectThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    addressToReconnectMonitorMap.forEach((address, reconnectMonitorData) ->
                            log.info("Trying to reconnect to node {} with type {}. Retries: {}, DisconnectTime: {}", address,
                                    Optional.ofNullable(reconnectMonitorData.getNodeType()).map(NodeType::toString).orElse("unknown"),
                                    reconnectMonitorData.getRetriesNumber(), reconnectMonitorData.getDisconnectTime())
                    );
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }, "MONITOR RECONNECT " + socketType.name());
        monitorReconnectThread.start();
        return monitorReconnectThread;
    }
}
