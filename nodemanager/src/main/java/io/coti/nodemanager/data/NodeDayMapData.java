package io.coti.nodemanager.data;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.data.interfaces.IEntity;
import lombok.Data;
import org.apache.commons.lang3.tuple.Pair;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.concurrent.ConcurrentSkipListMap;

@Data
public class NodeDayMapData implements IEntity {

    private static final long serialVersionUID = 7144081521147671251L;
    private Hash nodeHash;
    private NodeType nodeType;
    private Instant activationDateTime;
    private ConcurrentSkipListMap<LocalDate, Hash> nodeDayMap;
    private NetworkNodeStatus nodeStatus;
    private Pair<LocalDate, Hash> statusChainRef;
    private boolean isChainHead;

    private NodeDayMapData() {
    }

    public NodeDayMapData(Hash nodeHash, Instant activationDateTime) {
        this.nodeHash = nodeHash;
        this.activationDateTime = activationDateTime;
        nodeDayMap = new ConcurrentSkipListMap<>();
    }

    @Override
    public Hash getHash() {
        return nodeHash;
    }

    @Override
    public void setHash(Hash hash) {
        this.nodeHash = hash;
    }

    public Hash calculateNodeHistoryDataHash(LocalDate localDate) {
        return new Hash(ByteBuffer.allocate(nodeHash.getBytes().length + Long.BYTES).
                put(nodeHash.getBytes()).putLong(localDate.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()).array());
    }

}
