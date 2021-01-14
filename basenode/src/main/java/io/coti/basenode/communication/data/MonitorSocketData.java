package io.coti.basenode.communication.data;

import lombok.Data;
import org.zeromq.ZMQ;

@Data
public class MonitorSocketData {

    private ZMQ.Socket monitorSocket;
    private String monitorAddress;

    public MonitorSocketData(ZMQ.Socket monitorSocket, String monitorAddress) {
        this.monitorSocket = monitorSocket;
        this.monitorAddress = monitorAddress;
    }
}
