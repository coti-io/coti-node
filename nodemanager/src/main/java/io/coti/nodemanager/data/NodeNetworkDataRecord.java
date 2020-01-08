package io.coti.nodemanager.data;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.NetworkNodeData;
import lombok.Data;
import org.apache.commons.lang3.tuple.Pair;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

@Data
public class NodeNetworkDataRecord implements Serializable {

    private static final long serialVersionUID = 7814283160669885668L;

    private Hash hash;
    private Instant recordTime;
    private NetworkNodeStatus nodeStatus;
    private NetworkNodeData networkNodeData;
    private Pair<LocalDate, Hash> statusChainRef;

    public NodeNetworkDataRecord() {
    }

    public NodeNetworkDataRecord(Instant recordTime, NetworkNodeStatus nodeStatus, NetworkNodeData networkNodeData) {
        this.recordTime = recordTime;
        this.nodeStatus = nodeStatus;
        this.networkNodeData = networkNodeData;
        this.hash = calculateHash();
        this.statusChainRef = Pair.of(recordTime.atZone(ZoneId.of("UTC")).toLocalDate(), this.hash);
    }

    private Hash calculateHash() {
        return new Hash(ByteBuffer.allocate(Long.BYTES).putLong(this.recordTime.toEpochMilli()).array());
    }
}
