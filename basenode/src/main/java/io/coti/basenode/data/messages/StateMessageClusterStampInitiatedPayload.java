package io.coti.basenode.data.messages;

import lombok.Data;

import java.nio.ByteBuffer;

@Data
public class StateMessageClusterStampInitiatedPayload extends MessagePayload {

    private long delay;

    public StateMessageClusterStampInitiatedPayload() {
    }

    public StateMessageClusterStampInitiatedPayload(long delay) {
        super(GeneralMessageType.CLUSTER_STAMP_INITIATED);
        this.delay = delay;
    }

    @Override
    public byte[] getMessageInBytes() {
        byte[] broadcastTypeBytes = generalMessageType.name().getBytes();
        return ByteBuffer.allocate(broadcastTypeBytes.length + Long.BYTES).put(broadcastTypeBytes).putLong(delay).array();
    }

}
