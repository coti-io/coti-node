package io.coti.trustscore.config.rules;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "user")
public class UserScoresByType {

    @XmlAttribute(name = "type")
    private String type;

    private InitialTrustScore initialTrustScore;
    private TransactionEventsScore transactionEventScoreList;
    private BehaviorEventsScore behaviorEventsScoreList;
    private BehaviorHighFrequencyEventsScore behaviorHighFrequencyEventsScore;

    public InitialTrustScore getInitialTrustScore() {
        return initialTrustScore;
    }


    public String getType() {
        return type;
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

    public BehaviorHighFrequencyEventsScore getHighFrequencyEventScore() {
        return behaviorHighFrequencyEventsScore;
    }

    @XmlElement(name = "behaviorHighFrequencyEventsScore")
    public void setDisputedEventsScore(BehaviorHighFrequencyEventsScore behaviorHighFrequencyEventsScore) {
        this.behaviorHighFrequencyEventsScore = behaviorHighFrequencyEventsScore;
    }

}
