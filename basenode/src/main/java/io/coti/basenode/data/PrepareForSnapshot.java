package io.coti.basenode.data;

import io.coti.basenode.data.interfaces.IEntity;
import io.coti.basenode.data.interfaces.IPropagatable;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import lombok.Data;

@Data
public class PrepareForSnapshot implements IEntity, IPropagatable, ISignable, ISignValidatable {

    private Hash hash;
    private Long lastDspConfirmed;
    private Hash zeroSpendHash;
    private SignatureData zeroSpendSignature;

    public PrepareForSnapshot(Long lastDspConfirmed) {
        this.hash = new Hash(lastDspConfirmed);
        this.lastDspConfirmed = lastDspConfirmed;
    }

    @Override
    public Hash getHash() {
        return hash;
    }

    @Override
    public void setHash(Hash hash) {
        this.hash = hash;
    }

    @Override
    public SignatureData getSignature() {
        return zeroSpendSignature;
    }

    @Override
    public Hash getSignerHash() {
        return zeroSpendHash;
    }

    @Override
    public void setSignerHash(Hash signerHash) {
        zeroSpendHash = signerHash;
    }

    @Override
    public void setSignature(SignatureData signature) {
        zeroSpendSignature = signature;
    }
}