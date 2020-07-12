package io.coti.basenode.communication;

import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import org.zeromq.ZMQException;

public class ZeroMQUtils {

    private ZeroMQUtils() {

    }

    public static int bindToRandomPort(ZMQ.Socket socket) {
        boolean success = false;
        int port = 10000;
        while (!success) {
            try {
                success =  socket.bind("tcp://*:" + port);
                if(!success) {
                    port++;
                }
            } catch (ZMQException exception) {
                port++;
            }
        }
        return port;
    }

    public static ZMQ.Socket createAndConnectMonitorSocket(ZMQ.Context zeroMQContext, ZMQ.Socket socket) {
        String monitorAddress = "inproc://" + socket.getSocketType().name();
        socket.monitor(monitorAddress, ZMQ.EVENT_ALL);
        ZMQ.Socket monitorSocket = zeroMQContext.socket(SocketType.PAIR);
        monitorSocket.connect(monitorAddress);
        return monitorSocket;
    }
}
