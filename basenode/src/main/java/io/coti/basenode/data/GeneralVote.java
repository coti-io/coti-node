package io.coti.basenode.data;

import lombok.Data;

import java.io.Serializable;

@Data
public class GeneralVote implements Serializable {
// todo not finished
    private static final long serialVersionUID = -7343136665215500025L;
    protected boolean voteValid;
    protected Hash voterHash;
    protected SignatureData signature;

    protected GeneralVote() {
    }

    public GeneralVote(GeneralVote generalVote) {
        this.voteValid = generalVote.isVoteValid();
        this.voterHash = generalVote.getVoterHash();
        this.signature = generalVote.getSignature();
    }

}
