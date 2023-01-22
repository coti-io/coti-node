package io.coti.trustscore.services.calculationservices;

import io.coti.trustscore.config.rules.EventScore;
import io.coti.trustscore.services.calculationservices.interfaces.IScoreCalculator;
import io.coti.trustscore.utils.MathCalculation;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.AbstractMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ScoreCalculator<T extends EventScore> implements IScoreCalculator {

    private Map<T, String> eventScoresToScoreCalculationFormulaMap;

    public ScoreCalculator() {
    }

    public ScoreCalculator(Map<T, String> eventScoresToScoreCalculationFormulaMap) {
        this.eventScoresToScoreCalculationFormulaMap = eventScoresToScoreCalculationFormulaMap;
    }

    public Map<T, Double> calculate() {
        return eventScoresToScoreCalculationFormulaMap.entrySet().stream().map(e ->
                        new AbstractMap.SimpleEntry<>(e.getKey(), MathCalculation.evaluateExpression(e.getValue())))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public MutablePair<T, Double> calculateEntry(MutablePair<T, String> eventScoresToScoreCalculationFormulaPair) {
        return new MutablePair<>(eventScoresToScoreCalculationFormulaPair.getKey(), MathCalculation.evaluateExpression(eventScoresToScoreCalculationFormulaPair.getValue()));
    }
}
