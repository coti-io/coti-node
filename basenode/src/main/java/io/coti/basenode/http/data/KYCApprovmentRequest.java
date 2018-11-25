package io.coti.basenode.http.data;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import io.coti.basenode.http.Request;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Data
public class KYCApprovmentRequest extends Request implements ISignable, ISignValidatable {

    @NotNull
    private Hash userHash;
    @NotNull
    @Valid
    private SignatureData signature;
    @NotNull
    private LocalDateTime creationTime;

    public KYCApprovmentRequest(@NotNull Hash userHash, @NotNull @Valid SignatureData signature) {
        this.userHash = userHash;
        this.signature = signature;
        this.creationTime = LocalDateTime.now(ZoneOffset.UTC);
    }

    @Override
    public Hash getSignerHash() {
        return userHash;
    }

    @Override
    public void setSignerHash(Hash signerHash) {
        userHash = signerHash;
    }
}
