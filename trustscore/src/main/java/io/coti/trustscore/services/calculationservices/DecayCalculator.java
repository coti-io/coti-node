package io.coti.trustscore.services.calculationservices;

import io.coti.trustscore.config.rules.EventScore;
import io.coti.trustscore.services.calculationservices.interfaces.IDecayCalculator;
import io.coti.trustscore.services.calculationservices.interfaces.IEventDecay;
import io.coti.trustscore.utils.MathCalculation;
import org.apache.commons.lang3.tuple.MutablePair;

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
        return eventScoresToOldValueMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e ->
                MathCalculation.evaluateExpression(e.getKey().getDecay().replace("T", String.valueOf(numberOfDecays))) * e.getValue()));
    }

    public MutablePair<T, Double> calculateEntry(IEventDecay decayEvent, int numberOfDecays) {
        return new MutablePair(decayEvent.getEventScore(),
                MathCalculation.evaluateExpression(decayEvent.getEventScore().getDecay().replace("T", String.valueOf(numberOfDecays)))
                        * decayEvent.getEventContributionValue());
    }
}


