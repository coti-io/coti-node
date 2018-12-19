package io.coti.trustscore.services.calculationServices;

import io.coti.trustscore.config.rules.EventScore;
import io.coti.trustscore.services.calculationServices.interfaces.IDecayCalculator;
import io.coti.trustscore.services.calculationServices.interfaces.IEventDecay;
import io.coti.trustscore.utils.MathCalculation;
import javafx.util.Pair;

import java.util.Map;
import java.util.stream.Collectors;

public class DecayCalculator<T extends EventScore> implements IDecayCalculator {
    private Map<T, Double> eventScoresToOldValueMap;

    public DecayCalculator() {
    }

    public DecayCalculator(Map<T, Double> eventScoresToOldValueMap) {
        this.eventScoresToOldValueMap = eventScoresToOldValueMap;
    }

    @Override
    public Map<T, Double> calculate(int numberOfDecays) {
        return eventScoresToOldValueMap.entrySet().stream().collect(Collectors.toMap(e -> e.getKey(), e ->
                MathCalculation.evaluateExpression(e.getKey().getDecayFormula().replaceAll("T", String.valueOf(numberOfDecays))) * e.getValue()));
    }

    public Pair<T, Double> calculateEntry(IEventDecay decayEvent, int numberOfDecays) {
        return new Pair(decayEvent.getEventScore(),
                MathCalculation.evaluateExpression(decayEvent.getEventScore().getDecayFormula().replaceAll("T", String.valueOf(numberOfDecays)))
                        * decayEvent.getEventContributionValue());
    }
}


