package io.coti.basenode.data.messages;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.IPropagatable;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import lombok.Data;

import java.time.Instant;

@Data
public abstract class MessageData implements IPropagatable, ISignable, ISignValidatable {

    private Instant createTime;
    private Hash hash;
    private Hash signerHash;
    private SignatureData signatureData;

    public MessageData(Instant createTime) {
        this.createTime = createTime;
    }

    public MessageData() {
    }

    public abstract byte[] getMessageInBytes();

    @Override
    @JsonIgnore
    public SignatureData getSignature() {
        return signatureData;
    }

    @Override
    public void setSignature(SignatureData signature) {
        this.signatureData = signature;
    }

    @Override
    public Hash getSignerHash() {
        return signerHash;
    }

    @Override
    public void setSignerHash(Hash signerHash) {
        this.signerHash = signerHash;
    }
}