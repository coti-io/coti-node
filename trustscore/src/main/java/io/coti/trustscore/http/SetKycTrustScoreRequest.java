package io.coti.trustscore.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.http.Request;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class SetKycTrustScoreRequest extends Request {
    @NotNull
    public Hash userHash;
    @NotNull
    public String userType;
    @NotNull
    public @Valid SignatureData signature;
    @NotNull
    public double kycTrustScore;
}