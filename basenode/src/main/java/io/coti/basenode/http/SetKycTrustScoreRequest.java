package io.coti.basenode.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class SetKycTrustScoreRequest extends Request {
    @NotNull
    public Hash userHash;
    @NotNull
    public @Valid SignatureData signature;
    @NotNull
    public double kycTrustScore;
}