package io.coti.basenode.http;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class GetUserTrustScoreResponse extends BaseResponse {

    private String userHash;
    private double trustScore;
    private String userType;

    public GetUserTrustScoreResponse(String userHash, double trustScore, String userType) {
        this.userHash = userHash;
        this.trustScore = trustScore;
        this.userType = userType;
    }

}