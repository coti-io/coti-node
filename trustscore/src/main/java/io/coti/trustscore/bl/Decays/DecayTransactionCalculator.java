package io.coti.trustscore.bl.Decays;

import io.coti.trustscore.rulesData.TransactionEventScore;
import io.coti.trustscore.utils.MathCalculation;
import javafx.util.Pair;

import java.util.Map;
import java.util.stream.Collectors;

public class DecayTransactionCalculator implements IDecayCalculator {
    private Map<TransactionEventScore, Double> eventScoresToOldValueMap;

    public DecayTransactionCalculator() {
    }

    public DecayTransactionCalculator(Map<TransactionEventScore, Double> eventScoresToOldValueMap) {
        this.eventScoresToOldValueMap = eventScoresToOldValueMap;
    }

    public Map<TransactionEventScore, Double> calculate(int numberOfDecays) {
        return eventScoresToOldValueMap.entrySet().stream().collect(Collectors.toMap(e -> e.getKey(), e ->
                MathCalculation.evaluteExpression(e.getKey().getDecay().replaceAll("T", String.valueOf(numberOfDecays))) * e.getValue()));
    }

    public Pair<TransactionEventScore, Double> calculateEntry(TransactionEventDecay decayEvent, int numberOfDecays) {
        return new Pair(decayEvent.getTransactionEventScore(),
                MathCalculation.evaluteExpression(decayEvent.getTransactionEventScore().getDecay().replaceAll("T", String.valueOf(numberOfDecays)))
                        * decayEvent.getEventContributionValue());
    }
}


