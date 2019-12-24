package io.coti.trustscore.http;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class SetUserZeroTrustFlagSignedRequest extends SignedRequest {
    @NotNull
    private boolean zeroTrustFlag;
}

