package io.coti.basenode.data.messages;

import io.coti.basenode.data.Hash;
import lombok.Data;

import java.nio.ByteBuffer;

@Data
public class GeneralVoteClusterStampIndexPayload extends MessagePayload {

    private boolean vote;
    private Hash voteHash;

    public GeneralVoteClusterStampIndexPayload() {
    }

    public GeneralVoteClusterStampIndexPayload(Hash voteHash, boolean vote) {
        super(GeneralMessageType.CLUSTER_STAMP_INDEX_VOTE);
        this.vote = vote;
        this.voteHash = voteHash;
    }

    @Override
    public byte[] getMessageInBytes() {
        byte[] broadcastTypeBytes = generalMessageType.name().getBytes();
        byte[] voteHashInBytes = voteHash.getBytes();
        return ByteBuffer.allocate(broadcastTypeBytes.length + voteHashInBytes.length + 1).put(broadcastTypeBytes).put(voteHashInBytes).put(vote ? (byte) 1 : (byte) 0).array();
    }

}
