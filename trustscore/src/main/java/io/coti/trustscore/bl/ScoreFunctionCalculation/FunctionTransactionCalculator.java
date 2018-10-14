package io.coti.trustscore.bl.ScoreFunctionCalculation;
import io.coti.trustscore.bl.FunctionCalculator;
import io.coti.trustscore.rulesData.TransactionEventScore;
import io.coti.trustscore.utils.MathCalculation;
import javafx.util.Pair;

import java.util.AbstractMap;
import java.util.Map;
import java.util.stream.Collectors;

public class FunctionTransactionCalculator implements FunctionCalculator {
    private Map<TransactionEventScore, String> eventScoresToScoreFunctionStringMap;

    public FunctionTransactionCalculator() {

    }

    public FunctionTransactionCalculator(Map<TransactionEventScore, String> eventScoresToScoreFunctionStringMap) {
        this.eventScoresToScoreFunctionStringMap = eventScoresToScoreFunctionStringMap;
        //this.mathCalculation = new MathCalculation(eventScore.getScoreFunction());
    }

    public Map<TransactionEventScore, Double> calculate() {
        return eventScoresToScoreFunctionStringMap.entrySet().stream().map(e->
                new AbstractMap.SimpleEntry<>(e.getKey(), MathCalculation.evaluteExpression(e.getValue())))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public Pair<TransactionEventScore, Double> calculateEntry(Pair<TransactionEventScore, String> eventScoresToFunctionStringPair) {
        return new Pair<>(eventScoresToFunctionStringPair.getKey(), MathCalculation.evaluteExpression(eventScoresToFunctionStringPair.getValue()));
    }
}
