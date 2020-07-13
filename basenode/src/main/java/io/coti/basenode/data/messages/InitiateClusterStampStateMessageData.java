package io.coti.basenode.data.messages;

import lombok.Data;

import java.nio.ByteBuffer;
import java.time.Instant;

@Data
public class InitiateClusterStampStateMessageData extends ClusterStampStateMessageData {

    private long delay;
    private long timeout;

    private InitiateClusterStampStateMessageData() {
    }

    public InitiateClusterStampStateMessageData(long delay, long timeout, Instant createTime) {
        super(createTime);
        this.delay = delay;
        this.timeout = timeout;
    }

    @Override
    public byte[] getMessageInBytes() {
        byte[] typeBytes = StateMessageType.CLUSTER_STAMP_INITIATED.name().getBytes();
        return ByteBuffer.allocate(typeBytes.length + Long.BYTES + Long.BYTES).put(typeBytes).putLong(delay).putLong(timeout).array();
    }

}
