package io.coti.trustscore.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.http.interfaces.IRequest;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
public class SetKycTrustScoreRequest implements IRequest {
    @NotNull
    public Hash userHash;
    @NotNull
    public String userType;
    @NotNull
    public @Valid SignatureData signature;
    @NotNull
    public double kycTrustScore;
}