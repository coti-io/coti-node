package io.coti.basenode.data;

import io.coti.basenode.data.interfaces.IPropagatable;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import lombok.Data;

@Data
public abstract class ClusterStampStateData implements IPropagatable, ISignable, ISignValidatable {

    protected Hash clusterStampHash;
    protected long totalConfirmedTransactionsCount;
    protected Hash nodeHash;
    protected SignatureData nodeSignature;

    public ClusterStampStateData() {
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
        return nodeSignature;
    }

    @Override
    public Hash getSignerHash() {
        return nodeHash;
    }

    @Override
    public void setSignerHash(Hash hash) {
        nodeHash = hash;
    }

    @Override
    public void setSignature(SignatureData signature) {
        nodeSignature = signature;
    }
}