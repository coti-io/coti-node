package io.coti.basenode.data.messages;

import io.coti.basenode.data.Hash;
import lombok.Data;

import java.time.Instant;

@Data
public abstract class VoteMessageData extends MessageData {

    protected Hash voteHash;
    protected boolean vote;

    public VoteMessageData() {
    }

    public VoteMessageData(Hash voteHash, boolean vote) {
        this.voteHash = voteHash;
        this.vote = vote;
    }

    public VoteMessageData(Hash voteHash, boolean vote, Instant createTime) {
        super(createTime);
        this.voteHash = voteHash;
        this.vote = vote;
    }

}