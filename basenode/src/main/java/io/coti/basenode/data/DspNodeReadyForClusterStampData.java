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
public class DspNodeReadyForClusterStampData implements IPropagatable, ISignable, ISignValidatable {

    private Hash hash;
    private Long lastDspConfirmed;
    private List<FullNodeReadyForClusterStampData> fullNodeReadyForClusterStampDataList;
    private Hash dspNodeHash;
    private SignatureData dspNodeSignature;

    public DspNodeReadyForClusterStampData(Hash hash) {
        this.hash = hash;
        this.fullNodeReadyForClusterStampDataList = new ArrayList<>();
    }

    public DspNodeReadyForClusterStampData() {
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
        return dspNodeSignature;
    }

    @Override
    public Hash getSignerHash() {
        return dspNodeHash;
    }

    @Override
    public void setSignerHash(Hash signerHash) {
        dspNodeHash = signerHash;
    }

    @Override
    public void setSignature(SignatureData signature) {
        dspNodeSignature = signature;
    }
}