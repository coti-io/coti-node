package io.coti.nodemanager.data;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.NetworkNodeData;
import lombok.Data;
import org.apache.commons.lang3.tuple.Pair;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;

@Data
public class NodeNetworkDataRecord implements Serializable {

    private static final long serialVersionUID = 7814283160669885668L;
    private Hash hash;
    private Instant recordTime;
    private NetworkNodeStatus nodeStatus;
    private NetworkNodeData networkNodeData;
    private Pair<LocalDate, Hash> statusChainRef;
    private boolean notOriginalEvent;

    public NodeNetworkDataRecord() {
    }

    public NodeNetworkDataRecord(Instant recordTime, NetworkNodeStatus nodeStatus, NetworkNodeData networkNodeData) {
        this.recordTime = recordTime;
        this.nodeStatus = nodeStatus;
        this.networkNodeData = networkNodeData;
        this.hash = calculateHash();
    }

    private Hash calculateHash() {
        byte[] nodeStatusBytes = nodeStatus.toString().getBytes(StandardCharsets.UTF_8);
        return new Hash(ByteBuffer.allocate(Long.BYTES + nodeStatusBytes.length).putLong(this.recordTime.toEpochMilli()).put(nodeStatusBytes).array());
    }

    public Instant getRecordTime() {
        return recordTime.minusNanos(recordTime.getNano());
    }
}
