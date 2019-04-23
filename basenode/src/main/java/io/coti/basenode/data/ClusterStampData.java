package io.coti.basenode.data;

import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Data
public class ClusterStampData implements ISignable, ISignValidatable {

    private Hash zeroSpendServerHash;
    private List<byte[]> signatureMessage = new ArrayList<>();
    private int messageByteSize = 0;
    private SignatureData zeroSpendSignature;

    public ClusterStampData() {
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

    public void incrementMessageByteSize(long addedMessageByteSize) {
        messageByteSize += addedMessageByteSize;
    }
}