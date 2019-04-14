package io.coti.trustscore.config.rules;

import lombok.Data;

@Data
public class User {

    private String type;
    private String addressMapping;
    private InitialTrustScoreEventsScore initialTrustScore;
    private TransactionEventsScore transactionEventScore;
    private BehaviorEventsScore behaviorEventsScore;
    private BehaviorHighFrequencyEventsScore behaviorHighFrequencyEventsScore;
    private CompensableEventsScore compensableEventsScore;

    public TransactionEventsScore getBehaviorCumulativeScores() {
        return transactionEventScore;
    }

    public void setBehaviorCumulativeScores(TransactionEventsScore transactionEventScoreList) {
        this.transactionEventScore = transactionEventScoreList;
    }
}
