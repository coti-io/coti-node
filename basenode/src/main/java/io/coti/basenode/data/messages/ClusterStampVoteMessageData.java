package io.coti.basenode.data.messages;

import io.coti.basenode.data.Hash;
import lombok.Data;

import java.time.Instant;

@Data
public abstract class ClusterStampVoteMessageData extends VoteMessageData {

    protected ClusterStampVoteMessageData() {
    }

    protected ClusterStampVoteMessageData(Hash voteHash, boolean vote, Instant createTime) {
        super(voteHash, vote, createTime);
    }
}
