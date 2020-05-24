package io.coti.basenode.data.messages;

import lombok.Data;

import java.nio.ByteBuffer;

@Data
public class StateMessageLastClusterStampIndexPayload extends MessagePayload {

    private long lastIndex;

    public StateMessageLastClusterStampIndexPayload() {
    }

    public StateMessageLastClusterStampIndexPayload(long lastIndex) {
        super(GeneralMessageType.CLUSTER_STAMP_PREPARE_INDEX);
        this.lastIndex = lastIndex;
    }

    @Override
    public byte[] getMessageInBytes() {
        byte[] broadcastTypeBytes = generalMessageType.name().getBytes();
        return ByteBuffer.allocate(broadcastTypeBytes.length + Long.BYTES).put(broadcastTypeBytes).putLong(lastIndex).array();
    }
}
