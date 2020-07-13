package io.coti.basenode.data.messages;

import lombok.Data;

import java.nio.ByteBuffer;
import java.time.Instant;

@Data
public class LastIndexClusterStampStateMessageData extends ClusterStampStateMessageData {

    private long lastIndex;

    public LastIndexClusterStampStateMessageData() {
    }

    public LastIndexClusterStampStateMessageData(long lastIndex, Instant createTime) {
        super(createTime);
        this.lastIndex = lastIndex;
    }

    @Override
    public byte[] getMessageInBytes() {
        byte[] typeBytes = StateMessageType.CLUSTER_STAMP_PREPARE_INDEX.name().getBytes();
        return ByteBuffer.allocate(typeBytes.length + Long.BYTES).put(typeBytes).putLong(lastIndex).array();
    }

}