package io.coti.trustscore.http;

import io.coti.basenode.http.BaseResponse;
import lombok.Data;

@Data
public class SetKycTrustScoreResponse extends BaseResponse {
    private String userHash;
    private double kycTrustScore;

    public SetKycTrustScoreResponse(String userHash, double kycTrustScore) {
        super();
        this.userHash = userHash;
        this.kycTrustScore = kycTrustScore;

    }
}
