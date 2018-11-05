package io.coti.trustscore.services.calculationServices;

import io.coti.trustscore.config.rules.TransactionEventScore;
import javafx.util.Pair;

import java.util.HashMap;
import java.util.Map;

public class VectorFunctionTransactionCalculator {
    private TransactionFormulaCalculator functionTransactionCalculator;
    private Map<TransactionEventScore, Map<Long, String>> eventScoresToFormulaScoreMap;

    public VectorFunctionTransactionCalculator(Map<TransactionEventScore, Map<Long, String>> eventScoresToDatesScoreformulaMap) {
        this.eventScoresToFormulaScoreMap = eventScoresToDatesScoreformulaMap;
        this.functionTransactionCalculator = new TransactionFormulaCalculator();
    }

    public Map<TransactionEventScore, Map<Long, Double>> calculateVectorFunction() {
        Map<TransactionEventScore, Map<Long, Double>> eventScoresToValueByDayMap = new HashMap<>();
        for (Map.Entry<TransactionEventScore, Map<Long, String>> eventScoresToDatesScoreFormulaMapEntry : eventScoresToFormulaScoreMap.entrySet()) {
            TransactionEventScore transactionEventScore = eventScoresToDatesScoreFormulaMapEntry.getKey();
            Map<Long, String> dayToScoreScoreFormulaMap = eventScoresToDatesScoreFormulaMapEntry.getValue();
            Map<Long, Double> dailyScoreMap = new HashMap<>();
            for (Map.Entry<Long, String> dayToScoreFormulaMapEntry : dayToScoreScoreFormulaMap.entrySet()) {
                long dayTime = dayToScoreFormulaMapEntry.getKey();
                String formula = dayToScoreFormulaMapEntry.getValue();
                double dailyScore = functionTransactionCalculator.calculateEntry(new Pair<>(transactionEventScore, formula))
                        .getValue();
                dailyScoreMap.put(dayTime, dailyScore);
            }
            eventScoresToValueByDayMap.put(transactionEventScore, dailyScoreMap);
        }
        return eventScoresToValueByDayMap;
    }

}
