package io.coti.basenode.communication;

import io.coti.basenode.communication.data.MonitorSocketData;
import lombok.extern.slf4j.Slf4j;
import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import org.zeromq.ZMQException;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;

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

    public static ZMQ.Socket createAndConnectMonitorSocket(ZMQ.Context zeroMQContext, ZMQ.Socket socket) {
        String monitorAddress = getMonitorSocketAddress(socket);
        return createAndConnectMonitorSocket(zeroMQContext, socket, monitorAddress);
    }

    private static ZMQ.Socket createAndConnectMonitorSocket(ZMQ.Context zeroMQContext, ZMQ.Socket socket, String monitorAddress) {
        socket.monitor(monitorAddress, ZMQ.EVENT_ALL);
        ZMQ.Socket monitorSocket = zeroMQContext.socket(SocketType.PAIR);
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
            int errorCode = monitorSocket.base().errno();
            if (errorCode == ZMQ.Error.ETERM.getCode()) {
                contextTerminated.set(true);
            }
        }
    }
}
