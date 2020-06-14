package io.coti.basenode.communication;

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
                socket.bind("tcp://*:" + port);
                success = true;
            } catch (ZMQException exception) {
                port++;
            }
        }
        return port;
    }
}
