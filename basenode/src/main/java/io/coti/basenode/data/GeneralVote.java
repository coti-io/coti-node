package io.coti.basenode.data;

import io.coti.basenode.data.messages.GeneralVoteMessage;
import lombok.Data;

import java.io.Serializable;

@Data
public class GeneralVote implements Serializable {

    protected boolean voteValid;
    protected Hash voterHash;
    protected SignatureData signature;

    protected GeneralVote() {
    }

    public GeneralVote(GeneralVoteMessage generalVoteMessage) {
        this.voteValid = generalVoteMessage.isVote();
        this.voterHash = generalVoteMessage.getSignerHash();
        this.signature = generalVoteMessage.getSignature();
    }
}
