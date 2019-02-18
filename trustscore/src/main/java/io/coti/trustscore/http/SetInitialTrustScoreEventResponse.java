package io.coti.trustscore.http;


import io.coti.basenode.data.Hash;
import io.coti.basenode.http.BaseResponse;
import io.coti.trustscore.data.Enums.EventType;
import io.coti.trustscore.data.Enums.InitialTrustScoreType;
import lombok.Data;

@Data
public class SetInitialTrustScoreEventResponse extends BaseResponse {
    private String userHash;
    private int eventType;
    private String initialTrustScoreType;
    private double score;

    public SetInitialTrustScoreEventResponse(Hash userHash,
                                             EventType eventType,
                                             InitialTrustScoreType initialTrustScoreType,
                                             double score) {
        super();
        this.userHash = userHash.toHexString();
        this.eventType = eventType.getValue();
        this.initialTrustScoreType = initialTrustScoreType.toString();
        this.score = score;
    }
}