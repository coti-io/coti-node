package io.coti.basenode.data.messages;

import io.coti.basenode.data.Hash;
import lombok.Data;

import java.nio.ByteBuffer;

@Data
public class GeneralVoteClusterStampIndexPayload extends MessagePayload {

    private Hash voteHash;

    public GeneralVoteClusterStampIndexPayload() {
    }

    public GeneralVoteClusterStampIndexPayload(Hash voteHash) {
        super(GeneralMessageType.CLUSTER_STAMP_INDEX_VOTE);
        this.voteHash = voteHash;
    }

    @Override
    public byte[] getMessageInBytes() {
        byte[] broadcastTypeBytes = generalMessageType.name().getBytes();
        byte[] voteHashInBytes = voteHash.getBytes();
        return ByteBuffer.allocate(broadcastTypeBytes.length + voteHashInBytes.length).put(broadcastTypeBytes).put(voteHashInBytes).array();
    }

}
