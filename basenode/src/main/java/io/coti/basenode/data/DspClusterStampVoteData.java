package io.coti.basenode.data;

import io.coti.basenode.data.interfaces.IPropagatable;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import lombok.Data;

@Data
public class DspClusterStampVoteData implements IPropagatable, ISignable, ISignValidatable {

    public Hash clusterStampHash;
    public boolean validClusterStamp;
    public Hash voterDspHash;
    public SignatureData signature;

    private DspClusterStampVoteData() {
    }

    public DspClusterStampVoteData(Hash clusterStampHash, boolean validClusterStamp) {
        this.clusterStampHash = clusterStampHash;
        this.validClusterStamp = validClusterStamp;
    }

    @Override
    public Hash getHash() {
        return clusterStampHash;
    }

    @Override
    public void setHash(Hash hash) {
        this.clusterStampHash = hash;
    }

    @Override
    public SignatureData getSignature() {
        return signature;
    }

    @Override
    public Hash getSignerHash() {
        return voterDspHash;
    }

    @Override
    public void setSignerHash(Hash signerHash) {
        voterDspHash = signerHash;
    }

    @Override
    public void setSignature(SignatureData signature) {
        this.signature = signature;
    }

    public String toString() {
        return String.format("Cluster Stamp Hash= {}, Voter Hash= {}, IsValid= {}", clusterStampHash, voterDspHash, isValidClusterStamp());
    }
}
