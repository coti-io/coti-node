package io.coti.trustscore.data.Events;

import io.coti.trustscore.data.Enums.InitialTrustScoreType;
import io.coti.trustscore.http.InsertEventRequest;
import lombok.Data;

@Data
public class InitialTrustScoreEventsData extends EventData {
    private InitialTrustScoreType initialTrustScoreType;
    private double score;

    public InitialTrustScoreEventsData(InsertEventRequest request) {
        super(request);
        this.initialTrustScoreType = request.getInitialTrustScoreType();
        this.score = request.getScore();
        super.setSignatureData(request.getSignature());
    }
}
