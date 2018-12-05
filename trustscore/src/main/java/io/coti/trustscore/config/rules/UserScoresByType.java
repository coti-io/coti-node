package io.coti.trustscore.config.rules;

public class UserScoresByType {

    private String type;
    private String userID;
    private String addressMapping;
    private InitialTrustScoreEventsScore initialTrustScoreEventsScore;
    private TransactionEventsScore transactionEventScore;
    private BehaviorEventsScore behaviorEventsScore;
    private BehaviorHighFrequencyEventsScore behaviorHighFrequencyEventsScore;
    private CompensableEventsScore compensableEventsScore;


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public void setAddressMapping(String addressMapping) {
        this.addressMapping = addressMapping;
    }

    public InitialTrustScoreEventsScore getInitialTrustScoreEventsScore() {
        return initialTrustScoreEventsScore;
    }

    public void setInitialTrustScore(InitialTrustScoreEventsScore initialTrustScoreEventsScoreList) {
        this.initialTrustScoreEventsScore = initialTrustScoreEventsScoreList;
    }

    public TransactionEventsScore getBehaviorCumulativeScores() {
        return transactionEventScore;
    }

    public void setBehaviorCumulativeScores(TransactionEventsScore transactionEventScoreList) {
        this.transactionEventScore = transactionEventScoreList;
    }

    public BehaviorHighFrequencyEventsScore getHighFrequencyEventScore() {
        return behaviorHighFrequencyEventsScore;
    }

    public void setBehaviorHighFrequencyEventsScore(BehaviorHighFrequencyEventsScore behaviorHighFrequencyEventsScore) {
        this.behaviorHighFrequencyEventsScore = behaviorHighFrequencyEventsScore;
    }

    public BehaviorEventsScore getBehaviorEventsScore() {
        return behaviorEventsScore;
    }

    public void setBehaviorEventsScore(BehaviorEventsScore behaviorEventsScoreList) {
        this.behaviorEventsScore = behaviorEventsScoreList;
    }

    public CompensableEventsScore getCompensableEventsScore() {
        return compensableEventsScore;
    }

    public void setCompensableEventsScore(CompensableEventsScore compensableEventsScore) {
        this.compensableEventsScore = compensableEventsScore;
    }
}
