package io.coti.basenode.data.messages;

import io.coti.basenode.data.Hash;
import lombok.Data;

import java.nio.ByteBuffer;
import java.time.Instant;

@Data
public class ExecuteClusterStampStateMessageData extends ClusterStampStateMessageData {

    private Hash voteHash;
    private long lastIndex;

    public ExecuteClusterStampStateMessageData() {
    }

    public ExecuteClusterStampStateMessageData(Hash voteHash, long lastIndex, Instant createTime) {
        super(createTime);
        this.voteHash = voteHash;
        this.lastIndex = lastIndex;
    }

    @Override
    public byte[] getMessageInBytes() {
        byte[] typeBytes = StateMessageType.CLUSTER_STAMP_EXECUTE.name().getBytes();
        byte[] voteHashBytes = voteHash.getBytes();
        return ByteBuffer.allocate(typeBytes.length + voteHashBytes.length + Long.BYTES).put(typeBytes).put(voteHashBytes).putLong(lastIndex).array();
    }

}