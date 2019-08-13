package io.coti.basenode.communication;

import org.zeromq.ZMQ;
import org.zeromq.ZMQException;

public class ZeroMQUtils {

    public static int bindToRandomPort(ZMQ.Socket socket) {
        boolean success = false;
        int socketNumber = 10000;
        while (!success) {
            try {
                socket.bind("tcp://*:" + socketNumber);
                success = true;
            } catch (ZMQException exception) {
                socketNumber++;
            }
        }
        return socketNumber;
    }
}
