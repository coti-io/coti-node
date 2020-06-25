package io.coti.basenode.data;

import io.coti.basenode.data.messages.GeneralVoteMessage;
import lombok.Data;

import java.io.Serializable;

@Data
public class GeneralVote implements Serializable {

    protected boolean vote;
    protected Hash voterHash;
    protected SignatureData signature;

    protected GeneralVote() {
    }

    public GeneralVote(GeneralVoteMessage generalVoteMessage) {
        this.vote = generalVoteMessage.isVote();
        this.voterHash = generalVoteMessage.getSignerHash();
        this.signature = generalVoteMessage.getSignature();
    }
}
