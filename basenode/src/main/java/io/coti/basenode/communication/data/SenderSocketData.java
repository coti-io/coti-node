package io.coti.basenode.communication.data;

import io.coti.basenode.data.NodeType;
import lombok.Data;

import java.time.Instant;

@Data
public class SenderSocketData {

    private final int senderPort;
    private final NodeType nodeType;
    private Instant senderConnectionTime;

    public SenderSocketData(int port, NodeType nodeType) {
        senderPort = port;
        this.nodeType = nodeType;
        senderConnectionTime = Instant.now();
    }
}
