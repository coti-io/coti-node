package io.coti.basenode.http;

import lombok.Data;

@Data
public class GetUserTrustScoreResponse extends BaseResponse {
    private String userHash;
    private double trustScore;

    public GetUserTrustScoreResponse(String userHash, double trustScore) {
        this.userHash = userHash;
        this.trustScore = trustScore;
    }

}