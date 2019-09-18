package io.coti.basenode.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class GetTokenGenerationDataRequest extends Request implements ISignable, ISignValidatable {

    @NotNull
    private Hash senderHash;
    private Hash hash;
    private SignatureData signature;

    @Override
    public SignatureData getSignature() {
        return signature;
    }

    @Override
    public Hash getSignerHash() {
        return hash;
    }

    @Override
    public void setSignerHash(Hash hash) {
        this.hash = hash;
    }

    @Override
    public void setSignature(SignatureData signature) {
        this.signature = signature;
    }
}
