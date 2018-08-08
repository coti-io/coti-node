package io.coti.common.http;

import io.coti.common.data.TrustScoreData;
import lombok.Data;

@Data
public class SetKycTrustScoreResponse extends BaseResponse {
    private String userHash;
    private double trustScore;
    private double kycTrustScore;

    public SetKycTrustScoreResponse(TrustScoreData trustScoreData) {
        super();
        this.userHash = trustScoreData.getUserHash().toHexString();
        this.trustScore = trustScoreData.getTrustScore();
        this.kycTrustScore = trustScoreData.getKycTrustScore();

    }
}
