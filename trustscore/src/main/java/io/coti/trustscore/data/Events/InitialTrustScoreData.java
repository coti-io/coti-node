package io.coti.trustscore.data.Events;


import io.coti.trustscore.data.Enums.InitialTrustScoreType;
import lombok.Data;

import java.io.Serializable;

@Data
public class InitialTrustScoreData implements Serializable {

    private static final long serialVersionUID = -6523382550755760652L;
    private InitialTrustScoreType initialTrustScoreType;
    private double originalTrustScore;
    private double decayedTrustScore;

    public InitialTrustScoreData(InitialTrustScoreType initialTrustScoreType, double originalTrustScore, double decayedTrustScore) {
        this.initialTrustScoreType = initialTrustScoreType;
        this.originalTrustScore = originalTrustScore;
        this.decayedTrustScore = decayedTrustScore;
    }
}

// todo delete