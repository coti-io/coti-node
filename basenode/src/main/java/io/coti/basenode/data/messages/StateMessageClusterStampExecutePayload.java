package io.coti.basenode.data.messages;

import io.coti.basenode.data.Hash;
import lombok.Data;

import java.nio.ByteBuffer;

@Data
public class StateMessageClusterStampExecutePayload extends MessagePayload {

    private Hash voteHash;
    private long lastIndex;

    public StateMessageClusterStampExecutePayload() {
    }

    public StateMessageClusterStampExecutePayload(Hash voteHash, long lastIndex) {
        super(GeneralMessageType.CLUSTER_STAMP_EXECUTE);
        this.voteHash = voteHash;
        this.lastIndex = lastIndex;
    }

    @Override
    public byte[] getMessageInBytes() {
        byte[] broadcastTypeBytes = generalMessageType.name().getBytes();
        byte[] voteHashInBytes = voteHash.getBytes();
        return ByteBuffer.allocate(broadcastTypeBytes.length + voteHashInBytes.length + Long.BYTES).put(broadcastTypeBytes).put(voteHashInBytes).putLong(lastIndex).array();
    }

}
