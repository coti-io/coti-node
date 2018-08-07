package io.coti.common.http;

import io.coti.common.data.TrustScoreData;

public class SetKycTrustScoreResponse extends BaseResponse {
    private String userHash;
    private double trustScore;
    private double kycTrustScore;

    public SetKycTrustScoreResponse(TrustScoreData trustScoreData) {
        this.userHash = trustScoreData.getUserHash().toHexString();
        this.trustScore = trustScoreData.getTrustScore();
        this.kycTrustScore = trustScoreData.getKycTrustScore();
    }
}
