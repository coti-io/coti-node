package io.coti.trustscore.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.http.Request;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
public class SetKycTrustScoreRequest extends SignedRequest {
    private static final long serialVersionUID = -1016176727037169880L;
    @NotNull
    public String userType;
    @NotNull
    public double kycTrustScore;
}