package io.coti.basenode.data.messages;

import io.coti.basenode.data.Hash;
import lombok.Data;

import java.time.Instant;

@Data
public class GeneralVoteMessage extends GeneralMessage {

    private Hash voteHash;
    private boolean vote;

    public GeneralVoteMessage(MessagePayload messagePayload, Hash voteHash, boolean vote) {
        super(messagePayload);
        this.voteHash = voteHash;
        this.vote = vote;
    }

    public GeneralVoteMessage(MessagePayload messagePayload, Hash voteHash, boolean vote, Instant createTime) {
        super(messagePayload, createTime);
        this.voteHash = voteHash;
        this.vote = vote;
    }

    private GeneralVoteMessage() {
    }
}