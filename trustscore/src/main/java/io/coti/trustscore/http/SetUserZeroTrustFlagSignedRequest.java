package io.coti.trustscore.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.Request;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class SetUserZeroTrustFlagSignedRequest extends SignedRequest {

    private static final long serialVersionUID = 2245403882911115085L;
    @NotNull
    private boolean zeroTrustFlag;
}

