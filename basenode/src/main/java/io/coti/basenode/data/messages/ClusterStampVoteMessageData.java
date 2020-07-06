package io.coti.basenode.data.messages;

import io.coti.basenode.data.Hash;
import lombok.Data;

import java.time.Instant;

@Data
public abstract class ClusterStampVoteMessageData extends VoteMessageData {


    public ClusterStampVoteMessageData() {
    }

    public ClusterStampVoteMessageData(Hash voteHash, boolean vote) {
        super(voteHash, vote);
    }

    public ClusterStampVoteMessageData(Hash voteHash, boolean vote, Instant createTime) {
        super(voteHash, vote, createTime);
    }
}