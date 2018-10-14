package io.coti.trustscore.rulesData;

import io.coti.trustscore.data.Enums.UserType;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

public class UserScoresByType {

    @XmlAttribute(name = "type")
    public UserType type;

    private InitialTrustScore initialTrustScore;
    private TransactionEventsScore transactionEventScoreList;
    private BadEventsScore badEventScoreList;
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

    public BadEventsScore getBadEventsScore() {
        return badEventScoreList;
    }

    @XmlElement(name = "behaviorEventsScore")
    public void setBadEventsScore(BadEventsScore badEventScoreList) {
        this.badEventScoreList = badEventScoreList;
    }

    public DisputedEventsScore getDisputedEventsScore() {
        return disputedEventScoreList;
    }

    @XmlElement(name = "behaviorHighFrequencyEventsScore")
    public void setDisputedEventsScore(DisputedEventsScore disputedEventScoreList) {
        this.disputedEventScoreList = disputedEventScoreList;
    }

}
