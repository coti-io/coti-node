package io.coti.basenode.data.messages;

import lombok.Data;

import java.nio.ByteBuffer;

@Data
public class StateMessageClusterStampInitiatedPayload extends MessagePayload {

    private long delay;
    private long timeout;

    public StateMessageClusterStampInitiatedPayload() {
    }

    public StateMessageClusterStampInitiatedPayload(long delay, long timeout) {
        super(GeneralMessageType.CLUSTER_STAMP_INITIATED);
        this.delay = delay;
        this.timeout = timeout;
    }

    @Override
    public byte[] getMessageInBytes() {
        byte[] broadcastTypeBytes = generalMessageType.name().getBytes();
        return ByteBuffer.allocate(broadcastTypeBytes.length + Long.BYTES + Long.BYTES).put(broadcastTypeBytes).putLong(delay).putLong(timeout).array();
    }

}
