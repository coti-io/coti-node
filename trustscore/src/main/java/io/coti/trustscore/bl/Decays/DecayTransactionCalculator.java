package io.coti.trustscore.bl.Decays;
import io.coti.trustscore.bl.DecayCalculator;
import io.coti.trustscore.rulesData.TransactionEventScore;
import io.coti.trustscore.utils.MathCalculation;
import javafx.util.Pair;
import java.util.Map;
import java.util.stream.Collectors;

public class DecayTransactionCalculator implements DecayCalculator {
    private Map<TransactionEventScore, Double> eventScoresToOldValueMap;

    public DecayTransactionCalculator() {
    }

    public DecayTransactionCalculator(Map<TransactionEventScore, Double> eventScoresToOldValueMap) {
        this.eventScoresToOldValueMap = eventScoresToOldValueMap;
    }

    public Map<TransactionEventScore, Double> calculate(int numberOfDecays) {
        return eventScoresToOldValueMap.entrySet().stream().collect(Collectors.toMap(e->e.getKey(),e->
                MathCalculation.evaluteExpression(e.getKey().getDecay()) * numberOfDecays * e.getValue()));
    }

    public Pair<TransactionEventScore, Double> calculateEntry(Pair<TransactionEventScore, Double> eventScoresToDecayPair, int numberOfDecays) {
        return new Pair(eventScoresToDecayPair.getKey(),
                MathCalculation.evaluteExpression(eventScoresToDecayPair.getKey().getDecay()) * numberOfDecays * eventScoresToDecayPair.getValue());
    }

}
