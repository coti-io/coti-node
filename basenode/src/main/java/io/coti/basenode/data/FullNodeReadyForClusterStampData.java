package io.coti.basenode.data;

import io.coti.basenode.data.interfaces.IPropagatable;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class FullNodeReadyForClusterStampData implements IPropagatable, ISignable, ISignValidatable {

    private Hash hash;
    private Long lastDspConfirmed;
    private Hash fullNodeHash;
    private SignatureData fullNodeSignature;

    public FullNodeReadyForClusterStampData(Long lastDspConfirmed) {
        this.hash = new Hash(lastDspConfirmed);
        this.lastDspConfirmed = lastDspConfirmed;
    }

    public FullNodeReadyForClusterStampData() {
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
        return fullNodeSignature;
    }

    @Override
    public Hash getSignerHash() {
        return fullNodeHash;
    }

    @Override
    public void setSignerHash(Hash signerHash) {
        fullNodeHash = signerHash;
    }

    @Override
    public void setSignature(SignatureData signature) {
        fullNodeSignature = signature;
    }
}