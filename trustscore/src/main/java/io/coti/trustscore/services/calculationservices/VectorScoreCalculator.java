package io.coti.trustscore.services.calculationservices;

import io.coti.trustscore.config.rules.EventScore;
import javafx.util.Pair;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class VectorScoreCalculator<T extends EventScore> {
    private ScoreCalculator scoreCalculator;
    private Map<T, Map<Date, String>> eventScoresToFormulaScoreMap;

    public VectorScoreCalculator(Map<T, Map<Date, String>> eventScoresToDatesAndScoreFormulaMap) {
        this.eventScoresToFormulaScoreMap = eventScoresToDatesAndScoreFormulaMap;
        this.scoreCalculator = new ScoreCalculator();
    }

    public Map<T, Map<Date, Double>> calculateVectorScore() {
        Map<T, Map<Date, Double>> eventScoresToValueByDayMap = new ConcurrentHashMap<>();

        // Iterating on every event type
        for (Map.Entry<T, Map<Date, String>> eventScoresToDatesScoreFormulaMapEntry : eventScoresToFormulaScoreMap.entrySet()) {
            T eventScore = eventScoresToDatesScoreFormulaMapEntry.getKey();
            Map<Date, String> dayToScoreCalculateFormulaMap = eventScoresToDatesScoreFormulaMapEntry.getValue();
            Map<Date, Double> dailyScoreMap = new ConcurrentHashMap<>();

            // Iterating on every event day
            for (Map.Entry<Date, String> dayToScoreCalculateFormulaEntry : dayToScoreCalculateFormulaMap.entrySet()) {
                Date day = dayToScoreCalculateFormulaEntry.getKey();
                String formula = dayToScoreCalculateFormulaEntry.getValue();
                Pair<T, Double> eventScoreValue
                        = scoreCalculator.calculateEntry(new Pair<>(eventScore, formula));
                double dailyScore = eventScoreValue.getValue();
                dailyScoreMap.put(day, dailyScore);
            }
            eventScoresToValueByDayMap.put(eventScore, dailyScoreMap);
        }
        return eventScoresToValueByDayMap;
    }

}
