package io.coti.trustscore.config.rules;

import io.coti.trustscore.data.Enums.UserType;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

public class UserScoresByType {

    @XmlAttribute(name = "type")
    public UserType type;

    private InitialTrustScore initialTrustScore;
    private TransactionEventsScore transactionEventScoreList;
    private BehaviorEventsScore behaviorEventsScoreList;
    private DisputedEventsScore disputedEventScoreList;

    public InitialTrustScore getInitialTrustScore() {
        return initialTrustScore;
    }

    @XmlElement(name = "initialTrustScore")
    public void setInitialTrustScore(InitialTrustScore initialTrustScore) {
        this.initialTrustScore = initialTrustScore;
    }

    public TransactionEventsScore getTransactionEventsScore() {
        return transactionEventScoreList;
    }

    @XmlElement(name = "behaviorCumulativeScores")
    public void setTransactionEventsScore(TransactionEventsScore transactionEventScoreList) {
        this.transactionEventScoreList = transactionEventScoreList;
    }

    public BehaviorEventsScore getBehaviorEventsScore() {
        return behaviorEventsScoreList;
    }

    @XmlElement(name = "behaviorEventsScore")
    public void setBadEventsScore(BehaviorEventsScore badEventScoreList) {
        this.behaviorEventsScoreList = badEventScoreList;
    }

    public DisputedEventsScore getDisputedEventsScore() {
        return disputedEventScoreList;
    }

    @XmlElement(name = "behaviorHighFrequencyEventsScore")
    public void setDisputedEventsScore(DisputedEventsScore disputedEventScoreList) {
        this.disputedEventScoreList = disputedEventScoreList;
    }

}
