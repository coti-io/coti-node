package io.coti.basenode.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.coti.basenode.data.interfaces.IPropagatable;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class DspConsensusResult extends ConfirmationData implements IPropagatable, ISignable, ISignValidatable {

    private static final long serialVersionUID = -5825375825689935068L;
    private Hash transactionHash;
    private Hash zeroSpendServerHash;
    private long index;
    private Instant indexingTime;
    private SignatureData zeroSpendSignature;
    private List<DspVote> dspVotes;
    private boolean isDspConsensus;

    public DspConsensusResult(Hash transactionHash) {
        this.transactionHash = transactionHash;
    }

    private DspConsensusResult() {
    }

    @Override
    @JsonIgnore
    public Hash getHash() {
        return transactionHash;
    }

    @Override
    public void setHash(Hash hash) {
        this.transactionHash = hash;
    }

    @Override
    @JsonIgnore
    public SignatureData getSignature() {
        return zeroSpendSignature;
    }

    @Override
    @JsonIgnore
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