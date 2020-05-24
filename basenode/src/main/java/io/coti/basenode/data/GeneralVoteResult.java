package io.coti.basenode.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.coti.basenode.data.interfaces.IPropagatable;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import lombok.Data;

import java.util.List;

@Data
public class GeneralVoteResult implements IPropagatable, ISignable, ISignValidatable {
    // todo not finished
    private GeneralVoteType generalVoteType;
    private String generalVoteMessage;
    private Hash hash;
    private Hash signerHash;
    private SignatureData signatureData;
    private List<GeneralVote> generalVotes;
    private boolean isConsensus;

//    public GeneralVoteResult(){}

    private GeneralVoteResult() {
    }

    @Override
    @JsonIgnore
    public SignatureData getSignature() {
        return signatureData;
    }

    @Override
    public void setSignature(SignatureData signature) {
        this.signatureData = signature;
    }

    @Override
    @JsonIgnore
    public Hash getSignerHash() {
        return signerHash;
    }

    @Override
    public void setSignerHash(Hash signerHash) {
        this.signerHash = signerHash;
    }
}