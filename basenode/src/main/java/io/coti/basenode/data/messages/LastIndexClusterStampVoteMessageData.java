package io.coti.basenode.data.messages;

import io.coti.basenode.data.Hash;
import lombok.Data;

import java.nio.ByteBuffer;
import java.time.Instant;

@Data
public class LastIndexClusterStampVoteMessageData extends ClusterStampVoteMessageData {

    public LastIndexClusterStampVoteMessageData() {
    }

    public LastIndexClusterStampVoteMessageData(Hash voteHash, boolean vote, Instant createTime) {
        super(voteHash, vote, createTime);
    }

    @Override
    public byte[] getMessageInBytes() {
        byte[] typeBytes = VoteMessageType.CLUSTER_STAMP_INDEX_VOTE.name().getBytes();
        return ByteBuffer.allocate(typeBytes.length + Long.BYTES).put(typeBytes).putLong(getCreateTime().toEpochMilli()).array();
    }

}