package io.coti.nodemanager.data;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.data.interfaces.IEntity;
import lombok.Data;

import java.nio.ByteBuffer;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.concurrent.ConcurrentSkipListSet;

@Data
public class NodeDailyActivityData implements IEntity {

    private static final long serialVersionUID = 7144081521147671251L;
    private Hash nodeHash;
    private NodeType nodeType;
    private ConcurrentSkipListSet<LocalDate> nodeDaySet;

    public NodeDailyActivityData(Hash nodeHash, NodeType nodeType) {
        this.nodeHash = nodeHash;
        this.nodeType = nodeType;
        nodeDaySet = new ConcurrentSkipListSet<>();
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
