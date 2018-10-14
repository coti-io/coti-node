package io.coti.trustscore.bl.ScoreFunctionCalculation;

import io.coti.trustscore.rulesData.TransactionEventScore;
import javafx.util.Pair;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class VectorFunctionTransactionCalculator {
    private FunctionTransactionCalculator functionTransactionCalculator;
    private Map<TransactionEventScore, Map<Date, String>> eventScoresToDatesScoreformulaMap;

    public VectorFunctionTransactionCalculator(Map<TransactionEventScore, Map<Date, String>> eventScoresToDatesScoreformulaMap) {
        this.eventScoresToDatesScoreformulaMap = eventScoresToDatesScoreformulaMap;
        this.functionTransactionCalculator = new FunctionTransactionCalculator();
    }

    public Map<TransactionEventScore, Map<Date, Double>> calculateVectorFunction() {
        Map<TransactionEventScore, Map<Date, Double>> eventScoresToValueByDayMap = new HashMap<>();
        for (Map.Entry<TransactionEventScore, Map<Date, String>> eventScoresToDatesScoreFormulaMapEntry : eventScoresToDatesScoreformulaMap.entrySet()) {
            TransactionEventScore transactionEventScore = eventScoresToDatesScoreFormulaMapEntry.getKey();
            Map<Date, String> dayToScoreMap = eventScoresToDatesScoreFormulaMapEntry.getValue();
            Map<Date, Double> dailyScoreMap = new HashMap<>();
            for (Map.Entry<Date, String> dayToScoreFormulaMapEntry : dayToScoreMap.entrySet()) {
                Date Day = dayToScoreFormulaMapEntry.getKey();
                String formula = dayToScoreFormulaMapEntry.getValue();
                double dailyScore = functionTransactionCalculator.calculateEntry(new Pair<>(transactionEventScore, formula))
                        .getValue();
                dailyScoreMap.put(Day, dailyScore);
            }
            eventScoresToValueByDayMap.put(transactionEventScore, dailyScoreMap);
        }
        //calculateTotalContributionPerEvent(eventScoresToValueByDayMap);
        return eventScoresToValueByDayMap;
    }

//    private void calculateTotalContributionPerEvent(Map<TransactionEventScore, Map<Date, Double>> eventScoresToValueByDayMap) {
//        Map<TransactionEventScore, Map<Integer, Double>> eventScoresToValueByDayNumberMap = replaceDatesByDayDiffs(eventScoresToValueByDayMap);
//    }
//
//
//    private Map<TransactionEventScore, Map<Integer, Double>> replaceDatesByDayDiffs(Map<TransactionEventScore, Map<Date, Double>> eventScoresToFunctionalScoreMap) {
//        Map<TransactionEventScore, Map<Integer, Double>> eventScoresToFunctionalScoreByDaysDiffMap = new HashMap<>();
//
//        eventScoresToFunctionalScoreMap.forEach((eventScore, scoreByDay) -> {
//            Map<Integer, Double> scoredSumByDay = new HashMap<>();
//            scoreByDay.forEach((day, score) -> {
//                {
//                    scoredSumByDay.put(calculateDaysdiffBetweenDates(day, new Date()), score);
//                }
//            });
//            eventScoresToFunctionalScoreByDaysDiffMap.put(eventScore, scoredSumByDay);
//        });
//        return eventScoresToFunctionalScoreByDaysDiffMap ;
//    }

}
