package io.coti.basenode.communication.data;

import io.coti.basenode.data.NodeType;
import lombok.Data;

@Data
public class SenderSocketData {

    private final int senderPort;
    private final NodeType nodeType;

    public SenderSocketData(int port, NodeType nodeType) {
        senderPort = port;
        this.nodeType = nodeType;
    }
}
