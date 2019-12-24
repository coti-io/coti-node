package io.coti.trustscore.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.http.Request;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
public class SignedRequest extends Request implements ISignValidatable {
    @NotNull
    private Hash userHash;
    @NotNull
    private @Valid SignatureData signature;

    private Hash signerHash;

    @Override
    public Hash getSignerHash() {
        return this.signerHash;
    }

    public void setSignerHash(Hash signerHash) {
        this.signerHash = signerHash;
    }
}