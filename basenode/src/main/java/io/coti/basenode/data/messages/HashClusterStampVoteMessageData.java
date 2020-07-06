package io.coti.basenode.data.messages;

import io.coti.basenode.data.Hash;
import lombok.Data;

import java.nio.ByteBuffer;
import java.time.Instant;

@Data
public class HashClusterStampVoteMessageData extends ClusterStampVoteMessageData {

    private Hash clusterStampHash;

    public HashClusterStampVoteMessageData() {
    }

    public HashClusterStampVoteMessageData(Hash clusterStampHash, Hash voteHash, boolean vote, Instant createTime) {
        super(voteHash, vote, createTime);
        this.clusterStampHash = clusterStampHash;
    }

    @Override
    public byte[] getMessageInBytes() {
        byte[] typeBytes = VoteMessageType.CLUSTER_STAMP_HASH_VOTE.name().getBytes();
        byte[] clusterStampHashBytes = clusterStampHash.getBytes();
        return ByteBuffer.allocate(typeBytes.length + clusterStampHashBytes.length + Long.BYTES).put(typeBytes).put(clusterStampHashBytes).putLong(getCreateTime().toEpochMilli()).array();
    }

}