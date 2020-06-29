package io.coti.basenode.data.messages;

import io.coti.basenode.data.Hash;
import lombok.Data;

import java.nio.ByteBuffer;

@Data
public class StateMessageClusterStampContinuePayload extends MessagePayload {

    private Hash voteHash;

    public StateMessageClusterStampContinuePayload() {
    }

    public StateMessageClusterStampContinuePayload(Hash voteHash) {
        super(GeneralMessageType.CLUSTER_STAMP_CONTINUE);
        this.voteHash = voteHash;
    }

    @Override
    public byte[] getMessageInBytes() {
        byte[] broadcastTypeBytes = generalMessageType.name().getBytes();
        byte[] voteHashInBytes = voteHash.getBytes();
        return ByteBuffer.allocate(broadcastTypeBytes.length + voteHashInBytes.length).put(broadcastTypeBytes).put(voteHashInBytes).array();
    }

}
