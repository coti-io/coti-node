package io.coti.basenode.communication.data;

import io.coti.basenode.communication.ZeroMQUtils;
import lombok.Data;
import org.zeromq.SocketType;
import org.zeromq.ZMQ;

@Data
public class SenderSocketData {

    private final ZMQ.Socket senderSocket;
    private final int senderPort;
    private final MonitorSocketData monitorSocketData;
    private Thread monitorThread;

    public SenderSocketData(ZMQ.Context zeroMQContext) {
        senderSocket = zeroMQContext.socket(SocketType.DEALER);
        senderSocket.setHWM(10000);
        senderSocket.setLinger(100);
        monitorSocketData = ZeroMQUtils.getMonitorSocketData(zeroMQContext, senderSocket);
        senderPort = ZeroMQUtils.bindToRandomPort(senderSocket);
    }
}
