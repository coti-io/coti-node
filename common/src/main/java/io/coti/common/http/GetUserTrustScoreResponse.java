package io.coti.common.http;

import lombok.Data;

@Data
public class GetUserTrustScoreResponse extends Response {
    private String userHash;
    private double trustScore;

    public GetUserTrustScoreResponse(String userHash, double trustScore) {
        this.userHash = userHash;
        this.trustScore = trustScore;
    }

    public GetUserTrustScoreResponse(String message, String status) {
        super(message, status);
    }
}