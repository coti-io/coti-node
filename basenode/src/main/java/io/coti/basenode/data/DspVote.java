package io.coti.basenode.data;

import lombok.Data;

import java.io.Serializable;

@Data
public class DspVote implements Serializable {

    private static final long serialVersionUID = -2950462664712850605L;
    protected boolean validTransaction;
    protected Hash voterDspHash;
    protected SignatureData signature;

    protected DspVote() {
    }

    public DspVote(DspVote dspVote) {
        this.validTransaction = dspVote.isValidTransaction();
        this.voterDspHash = dspVote.getVoterDspHash();
        this.signature = dspVote.getSignature();
    }

}
