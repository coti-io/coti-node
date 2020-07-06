package io.coti.basenode.data.messages;

import lombok.Data;

import java.nio.ByteBuffer;
import java.time.Instant;

@Data
public class ContinueClusterStampStateMessageData extends ClusterStampStateMessageData {

    private ContinueClusterStampStateMessageData() {
    }

    public ContinueClusterStampStateMessageData(Instant createTime) {
        super(createTime);
    }

    @Override
    public byte[] getMessageInBytes() {
        byte[] typeBytes = StateMessageType.CLUSTER_STAMP_CONTINUE.name().getBytes();
        return ByteBuffer.allocate(typeBytes.length + Long.BYTES).put(typeBytes).putLong(getCreateTime().toEpochMilli()).array();
    }

}
