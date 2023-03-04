package io.coti.basenode.communication;

import io.coti.basenode.communication.data.ReconnectMonitorData;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.exceptions.CotiRunTimeException;
import lombok.extern.slf4j.Slf4j;
import org.zeromq.SocketType;
import org.zeromq.ZMQ;

import java.lang.reflect.Field;
import java.nio.channels.SocketChannel;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

@Slf4j
public class ZeroMQUtils {

    private ZeroMQUtils() {
    }

    private static final ConcurrentHashMap<SocketType, Integer> socketDisconnectMap = new ConcurrentHashMap<>();

    public static void initDisconnectMapForSocketType(SocketType socketType) {
        socketDisconnectMap.put(socketType, 0);
    }

    public static void closeSocket(ZMQ.Socket socket) {
        socket.setLinger(0);
        socket.close();
        log.info("ZeroMQ closing socket in thread ID: {} of thread: {}", Thread.currentThread().getId(), Thread.currentThread().getName());
    }

    public static String createAndStartMonitorOnSocket(ZMQ.Socket socket) {
        String monitorAddress = getMonitorSocketAddress(socket);
        if (!socket.monitor(monitorAddress, ZMQ.EVENT_ALL)) {
            throw new CotiRunTimeException("ZeroMQ was not able to initialize monitor on socket with address " + monitorAddress);
        }
        return monitorAddress;
    }

    public static ZMQ.Socket createAndConnectMonitorSocket(ZMQ.Context zeroMQContext, String monitorAddress) {
        ZMQ.Socket monitorSocket = zeroMQContext.socket(SocketType.PAIR);
        monitorSocket.setLinger(100);
        monitorSocket.connect(monitorAddress);
        log.info("ZeroMQ connected in thread ID: {} of thread: {}", Thread.currentThread().getId(), Thread.currentThread().getName());
        return monitorSocket;
    }

    private static String getMonitorSocketAddress(ZMQ.Socket socket) {
        return "inproc://" + socket.getSocketType().name() + Instant.now().toEpochMilli();
    }

    public static void getServerSocketEvent(ZMQ.Socket monitorSocket, SocketType socketType, AtomicBoolean monitorInitialized) {
        ZMQ.Event event = ZMQ.Event.recv(monitorSocket);
        if (event != null) {
            ZeroMQEvent zeroMQEvent = ZeroMQEvent.getEvent(event.getEvent());

            if (zeroMQEvent.isDisplayBeforeInit() || monitorInitialized.get()) {
                log.info("ZeroMQ getting server event {} on address: {}", zeroMQEvent, event.getAddress());
            }
            if (ZeroMQEvent.DISCONNECTED.equals(zeroMQEvent)) {
                String remoteAddress = getRemoteAddressFromEvent(event);
                updateDisconnectMap(socketType);
                log.info("ZeroMQ disconnected from remote address {}", remoteAddress);
            }

        } else {
            nullEventHandler(monitorSocket, socketType);
        }
    }

    private static void updateDisconnectMap(SocketType socketType) {
        synchronized (socketDisconnectMap) {
            socketDisconnectMap.put(socketType, socketDisconnectMap.get(socketType) + 1);
        }
    }

    public static int getSocketDisconnects(SocketType socketType) {
        int currentNumber;
        synchronized (socketDisconnectMap) {
            currentNumber = socketDisconnectMap.get(socketType);
            socketDisconnectMap.put(socketType, 0);
        }
        return currentNumber;
    }

    @SuppressWarnings("java:S3011")
    private static String getRemoteAddressFromEvent(ZMQ.Event event) {
        if (event.resolveValue() instanceof SocketChannel) {
            try {
                Field namedField = event.resolveValue().getClass().getDeclaredField("remoteAddress");
                namedField.setAccessible(true);
                return namedField.get(event.resolveValue()).toString();
            } catch (NoSuchFieldException | IllegalAccessException e) {
                log.error("ZeroMQ was not able to identify remote address, failed with exception: ", e);
                return "error";
            }
        }
        return event.getAddress();
    }

    private static void nullEventHandler(ZMQ.Socket monitorSocket, SocketType socketType) {
        int errorCode = monitorSocket.base().errno();
        if (errorCode == ZMQ.Error.ETERM.getCode()) {
            log.info("ZeroMQ {} context terminated", socketType);
            ZeroMQContext.setContextTerminated(true);
        }
    }

    public static void getClientServerEvent(ZMQ.Socket monitorSocket, SocketType socketType, AtomicBoolean monitorInitialized,
                                            Map<String, ReconnectMonitorData> addressToReconnectMonitorMap, Function<String, NodeType> getNodeTypeByAddress) {
        ZMQ.Event event = ZMQ.Event.recv(monitorSocket);
        if (event != null) {
            String address = event.getAddress();
            ZeroMQEvent zeroMQEvent = ZeroMQEvent.getEvent(event.getEvent());
            if (zeroMQEvent.isDisplayBeforeInit() || monitorInitialized.get()) {
                log.info("ZeroMQ getting client event {} on address: {}", zeroMQEvent, event.getAddress());
            }
            if (zeroMQEvent.equals(ZeroMQEvent.DISCONNECTED)) {
                String remoteAddress = getRemoteAddressFromEvent(event);
                log.info("ZeroMQ disconnected from remote address {}", remoteAddress);
                NodeType nodeType = getNodeTypeByAddress.apply(address);
                addToReconnectMonitor(addressToReconnectMonitorMap, address, nodeType);
            } else if (zeroMQEvent.equals(ZeroMQEvent.CONNECTED)) {
                removeFromReconnectMonitor(addressToReconnectMonitorMap, address);
            } else if (zeroMQEvent.equals(ZeroMQEvent.CONNECT_RETRIED)) {
                incrementRetriesInReconnectMonitor(addressToReconnectMonitorMap, address);
            }
        } else {
            nullEventHandler(monitorSocket, socketType);
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
