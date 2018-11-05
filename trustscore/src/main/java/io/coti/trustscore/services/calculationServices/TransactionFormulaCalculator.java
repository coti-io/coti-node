package io.coti.trustscore.services.calculationServices;

import io.coti.trustscore.config.rules.TransactionEventScore;
import io.coti.trustscore.services.calculationServices.interfaces.IEventFormulaCalculator;
import io.coti.trustscore.utils.MathCalculation;
import javafx.util.Pair;

import java.util.AbstractMap;
import java.util.Map;
import java.util.stream.Collectors;

public class TransactionFormulaCalculator implements IEventFormulaCalculator {
    private Map<TransactionEventScore, String> eventScoresToScoreFunctionStringMap;


    public TransactionFormulaCalculator(){}

    public TransactionFormulaCalculator(Map<TransactionEventScore, String> eventScoresToScoreFunctionStringMap) {
        this.eventScoresToScoreFunctionStringMap = eventScoresToScoreFunctionStringMap;
    }

    public Map<TransactionEventScore, Double> calculate() {
        return eventScoresToScoreFunctionStringMap.entrySet().stream().map(e ->
                new AbstractMap.SimpleEntry<>(e.getKey(), MathCalculation.evaluteExpression(e.getValue())))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public Pair<TransactionEventScore, Double> calculateEntry(Pair<TransactionEventScore, String> eventScoresToFunctionStringPair) {
        return new Pair<>(eventScoresToFunctionStringPair.getKey(), MathCalculation.evaluteExpression(eventScoresToFunctionStringPair.getValue()));
    }
}
