package io.coti.trustscore.http;

import io.coti.basenode.http.BaseResponse;
import io.coti.trustscore.data.TrustScoreData;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SetKycTrustScoreResponse extends BaseResponse {

    private String userHash;
    private double trustScore;
    private double kycTrustScore;

    public SetKycTrustScoreResponse(TrustScoreData trustScoreData) {
        super();
        this.userHash = trustScoreData.getUserHash().toHexString();
        this.kycTrustScore = trustScoreData.getKycTrustScore();

    }
}
