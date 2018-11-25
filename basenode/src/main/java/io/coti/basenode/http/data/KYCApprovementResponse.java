package io.coti.basenode.http.data;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import io.coti.basenode.http.Response;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
public class KYCApprovementResponse extends Response implements ISignable, ISignValidatable {
    @NotNull
    private Hash userHash;
    @NotNull
    @Valid
    private SignatureData signature;
    @NotNull
    private double trustScore;

    @Override
    public Hash getSignerHash() {
        return userHash;
    }

    @Override
    public void setSignerHash(Hash signerHash) {
        userHash = signerHash;
    }
}
