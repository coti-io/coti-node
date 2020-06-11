package io.coti.basenode.data;

import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Data
public class ClusterStampData implements ISignable, ISignValidatable {

    private Hash zeroSpendServerHash;
    private List<byte[]> signatureMessage = new ArrayList<>();
    private int messageByteSize = 0;
    private Instant createTime = Instant.now();
    private SignatureData zeroSpendSignature;

    @Override
    public SignatureData getSignature() {
        return zeroSpendSignature;
    }

    @Override
    public void setSignature(SignatureData signature) {
        zeroSpendSignature = signature;
    }

    @Override
    public Hash getSignerHash() {
        return zeroSpendServerHash;
    }

    @Override
    public void setSignerHash(Hash signerHash) {
        zeroSpendServerHash = signerHash;
    }

    public void incrementMessageByteSize(long addedMessageByteSize) {
        messageByteSize += addedMessageByteSize;
    }
}