package io.coti.trustscore.http;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class SetKycTrustScoreRequest extends SignedRequest {
    @NotNull
    private String userType;
    @NotNull
    private double kycTrustScore;
}