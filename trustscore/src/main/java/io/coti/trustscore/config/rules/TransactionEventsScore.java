package io.coti.trustscore.config.rules;

import io.coti.trustscore.data.enums.TransactionEventScoreType;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
public class TransactionEventsScore {

    private List<TransactionEventScore> transactionEventScoreList;

    public Map<TransactionEventScoreType, TransactionEventScore> getTransactionEventScoreMap() {
        return transactionEventScoreList.stream().collect(
                Collectors.toMap(t -> TransactionEventScoreType.enumFromString(t.getName()), t -> t));
    }

}