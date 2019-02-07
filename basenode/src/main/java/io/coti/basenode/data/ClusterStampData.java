package io.coti.basenode.data;

import io.coti.basenode.data.interfaces.IPropagatable;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Data
public class ClusterStampData implements IPropagatable, ISignable, ISignValidatable {

    private Hash hash;
    private Long lastDspConfirmed;
    private Map<Hash, BigDecimal> balanceMap;
    private List<DspReadyForClusterStampData> dspReadyForClusterStampDataList;
    private List<Hash> unconfirmedTransactions;
    private ClusterStampConsensusResult clusterStampConsensusResult;
    private Hash zeroSpendHash;
    private SignatureData zeroSpendSignature;

    public ClusterStampData(Hash hash) {
        this.hash = hash;
        this.dspReadyForClusterStampDataList = new ArrayList<>();
    }

    public ClusterStampData() {
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