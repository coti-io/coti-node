package io.coti.trustscore.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.http.interfaces.IRequest;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class GetTransactionTrustScoreRequest implements ISignValidatable, IRequest {

    @NotNull
    private Hash userHash;
    @NotNull
    private Hash transactionHash;
    @NotNull
    private SignatureData userSignature;

    @Override
    public SignatureData getSignature() {
        return userSignature;
    }

    @Override
    public Hash getSignerHash() {
        return userHash;
    }
}
