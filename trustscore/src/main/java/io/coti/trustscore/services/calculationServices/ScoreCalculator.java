package io.coti.trustscore.services.calculationServices;

import io.coti.trustscore.config.rules.EventScore;
import io.coti.trustscore.services.calculationServices.interfaces.IScoreCalculator;
import io.coti.trustscore.utils.MathCalculation;
import javafx.util.Pair;

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

    public Pair<T, Double> calculateEntry(Pair<T, String> eventScoresToScoreCalculationFormulaPair) {
        return new Pair<>(eventScoresToScoreCalculationFormulaPair.getKey(), MathCalculation.evaluateExpression(eventScoresToScoreCalculationFormulaPair.getValue()));
    }
}
