package io.coti.trustscore.config.rules;

import io.coti.trustscore.data.Enums.TransactionEventScoreType;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TransactionEventsScore {

    private List<TransactionEventScore> transactionEventScoreList;

    public List<TransactionEventScore> getTransactionEventScoreList() {
        return transactionEventScoreList;
    }

    public void setTransactionEventScoreList(List<TransactionEventScore> transactionEventScoreList) {
        this.transactionEventScoreList = transactionEventScoreList;
    }

    public Map<TransactionEventScoreType, TransactionEventScore> getTransactionEventScoreMap() {
        return transactionEventScoreList.stream().collect(
                Collectors.toMap(t -> TransactionEventScoreType.enumFromString(t.getName()), t -> t));
    }

}