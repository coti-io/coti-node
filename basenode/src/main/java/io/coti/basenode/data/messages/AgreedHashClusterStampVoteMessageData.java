package io.coti.basenode.data.messages;

import io.coti.basenode.data.Hash;
import lombok.Data;

import java.nio.ByteBuffer;
import java.time.Instant;

@Data
public class AgreedHashClusterStampVoteMessageData extends ClusterStampVoteMessageData {

    private AgreedHashClusterStampVoteMessageData() {
    }

    public AgreedHashClusterStampVoteMessageData(Hash clusterStampHash, boolean vote, Instant createTime) {
        super(clusterStampHash, vote, createTime);
    }

    @Override
    public byte[] getMessageInBytes() {
        byte[] typeBytes = VoteMessageType.CLUSTER_STAMP_AGREED_HASH_HISTORY_NODE.name().getBytes();
        return ByteBuffer.allocate(typeBytes.length).put(typeBytes).array();
    }

}
