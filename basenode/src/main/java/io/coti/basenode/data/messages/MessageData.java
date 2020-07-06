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

    protected Instant createTime;
    protected Hash hash;
    protected Hash signerHash;
    protected SignatureData signatureData;

    protected MessageData() {
    }

    protected MessageData(Instant createTime) {
        this.createTime = createTime;
    }

    @JsonIgnore
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
}
