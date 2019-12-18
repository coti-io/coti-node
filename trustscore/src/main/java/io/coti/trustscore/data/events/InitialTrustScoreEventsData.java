package io.coti.trustscore.data.events;

import io.coti.trustscore.data.enums.InitialTrustScoreType;
import io.coti.trustscore.http.InsertEventRequest;
import lombok.Data;

@Data
public class InitialTrustScoreEventsData extends EventData {

    private static final long serialVersionUID = 3646340117367057313L;
    private InitialTrustScoreType initialTrustScoreType;
    private double score;

    public InitialTrustScoreEventsData(InsertEventRequest request) {
        super(request);
        this.initialTrustScoreType = request.getInitialTrustScoreType();
        if (this.initialTrustScoreType.equals(InitialTrustScoreType.KYC)) {
            this.score = request.getScore() - 10.0;
        } else {
            this.score = request.getScore();
        }
        super.setSignatureData(request.getSignature());
    }
}
