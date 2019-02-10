package io.coti.basenode.data;

import io.coti.basenode.data.interfaces.IPropagatable;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Data
public abstract class ClusterStampStateData implements IPropagatable, ISignable, ISignValidatable {

    protected Hash clusterStampHash;
    protected long lastDspConfirmed;
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