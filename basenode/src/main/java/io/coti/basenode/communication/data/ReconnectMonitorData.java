package io.coti.basenode.communication.data;

import io.coti.basenode.data.NodeType;
import lombok.Data;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

@Data
public class ReconnectMonitorData {

    private NodeType nodeType;
    private AtomicInteger retriesNumber;
    private Instant disconnectTime;

    public ReconnectMonitorData(NodeType nodeType) {
        this.nodeType = nodeType;
        this.retriesNumber = new AtomicInteger();
        disconnectTime = Instant.now();
    }
}
