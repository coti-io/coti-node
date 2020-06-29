package io.coti.basenode.data;

import io.coti.basenode.data.messages.GeneralVoteMessage;
import lombok.Data;

import java.io.Serializable;
import java.time.Instant;

@Data
public class GeneralVote implements Serializable {

    private boolean vote;
    private Instant voteTime;
    private Hash voterHash;
    private SignatureData signature;

    protected GeneralVote() {
    }

    public GeneralVote(GeneralVoteMessage generalVoteMessage) {
        this.vote = generalVoteMessage.isVote();
        this.voteTime = generalVoteMessage.getCreateTime();
        this.voterHash = generalVoteMessage.getSignerHash();
        this.signature = generalVoteMessage.getSignature();
    }
}
