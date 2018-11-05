package io.coti.trustscore.config.rules;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@XmlRootElement(name = "behaviorCumulativeScores")
public class TransactionEventsScore {

    private List<TransactionEventScore> transactionEventScoreList;

    @XmlElement(name = "transactionScore")
    public List<TransactionEventScore> getTransactionEventScoreList() {
        return transactionEventScoreList;
    }

    public void setTransactionEventScoreList(List<TransactionEventScore> transactionEventScoreList) {
        this.transactionEventScoreList = transactionEventScoreList;
    }

    public Map<String, TransactionEventScore> getTransactionEventScoreMap() {
        return transactionEventScoreList.stream().collect(
                Collectors.toMap(t -> t.getName(), t -> t));
    }

}