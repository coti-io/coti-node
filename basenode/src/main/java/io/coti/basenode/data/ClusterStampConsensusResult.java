package io.coti.basenode.data;

import io.coti.basenode.data.interfaces.IPropagatable;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import lombok.Data;

import java.util.List;

@Data
public class ClusterStampConsensusResult implements IPropagatable, ISignable, ISignValidatable {

    private Hash clusterStampHash;
    private List<DspClusterStampVoteData> dspClusterStampVoteDataList;
    private boolean isDspConsensus;
    private Hash zeroSpendServerHash;
    private SignatureData zeroSpendSignature;

    public ClusterStampConsensusResult(Hash transactionHash) {
        this.clusterStampHash = transactionHash;
    }

    private ClusterStampConsensusResult() {
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