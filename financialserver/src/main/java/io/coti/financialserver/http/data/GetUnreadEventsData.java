package io.coti.financialserver.http.data;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.Instant;

@Data
public class GetUnreadEventsData implements ISignable, ISignValidatable {
    @NotNull
    private Hash userHash;
    @NotNull
    private @Valid SignatureData userSignature;
    @NotNull
    private Instant creationTime;

    @Override
    public SignatureData getSignature() {
        return userSignature;
    }

    @Override
    public Hash getSignerHash() {
        return userHash;
    }

    @Override
    public void setSignerHash(Hash signerHash) {
        this.userHash = signerHash;
    }

    @Override
    public void setSignature(SignatureData signature) {
        this.userSignature = signature;
    }
}
