package io.coti.basenode.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.coti.basenode.data.interfaces.IEntity;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class DspConsensusResult extends ConfirmationData implements Serializable, IEntity, ISignable, ISignValidatable {
    private Hash transactionHash;
    private Hash zeroSpendServerHash;
    private long index;
    private Date indexingTime;
    private SignatureData zeroSpendSignature;
    private List<DspVote> dspVotes;

    @JsonProperty
    private boolean isDspConsensus;

    public DspConsensusResult(Hash transactionHash) {
        this.transactionHash = transactionHash;
    }

    private DspConsensusResult() {
    }

    @Override
    public Hash getHash() {
        return transactionHash;
    }

    @Override
    public void setHash(Hash hash) {
        this.transactionHash = hash;
    }

    @Override
    public SignatureData getSignature() {
        return zeroSpendSignature;
    }

    @Override
    public Hash getSignerHash() {
        return zeroSpendServerHash;
    }

    @Override
    public void setSignerHash(Hash signerHash) {
        zeroSpendServerHash = signerHash;
    }

    @Override
    public void setSignature(SignatureData signature) {
        zeroSpendSignature = signature;
    }
}