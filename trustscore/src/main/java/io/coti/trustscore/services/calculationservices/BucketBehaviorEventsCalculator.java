package io.coti.trustscore.services.calculationservices;

import io.coti.trustscore.config.rules.BehaviorEventsScore;
import io.coti.trustscore.config.rules.RulesData;
import io.coti.trustscore.config.rules.SuspiciousEventScore;
import io.coti.trustscore.data.Buckets.BucketBehaviorEventsData;
import io.coti.trustscore.data.Enums.BehaviorEventsScoreType;
import io.coti.trustscore.data.Enums.UserType;
import io.coti.trustscore.data.Events.EventCountAndContributionData;
import io.coti.trustscore.utils.DatesCalculation;
import javafx.util.Pair;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class BucketBehaviorEventsCalculator extends BucketCalculator {

    private static Map<UserType, BehaviorEventsScore> userTypeToBehaviorEventsScoreMap;
    private BucketBehaviorEventsData bucketBehaviorEventsData;
    private BehaviorEventsScore behaviorEventsScore;

    public BucketBehaviorEventsCalculator(BucketBehaviorEventsData bucketBehaviorEventsData) {
        this.bucketBehaviorEventsData = bucketBehaviorEventsData;
        behaviorEventsScore = userTypeToBehaviorEventsScoreMap.get(bucketBehaviorEventsData.getUserType());
    }

    public static void init(RulesData rulesData) {
        userTypeToBehaviorEventsScoreMap = rulesData.getUserTypeToUserScoreMap().entrySet().stream().
                collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getBehaviorEventsScore()));
    }

    @Override
    public void setCurrentScores() {
        Map<SuspiciousEventScore, String> baseEventScoreToCalculationFormulaMap = new ConcurrentHashMap<>();

        for (Map.Entry<BehaviorEventsScoreType, SuspiciousEventScore> baseEventTypeToBaseEventScoreEntry : behaviorEventsScore.getBaseEventScoreMap().entrySet()) {

            SuspiciousEventScore suspiciousEventScore = baseEventTypeToBaseEventScoreEntry.getValue();
            String scoreFormulaCalculation = createBaseEventScoreFormula(baseEventTypeToBaseEventScoreEntry.getKey());

            if (scoreFormulaCalculation != null) {
                baseEventScoreToCalculationFormulaMap.put(suspiciousEventScore, scoreFormulaCalculation);
            }
        }

        ScoreCalculator<SuspiciousEventScore> scoreCalculator = new ScoreCalculator<>(baseEventScoreToCalculationFormulaMap);
        Map<SuspiciousEventScore, Double> baseEventScoreToCalculatedScoreMap = scoreCalculator.calculate();
        updateBucketScoresAfterCalculation(baseEventScoreToCalculatedScoreMap);

    }

    private void updateBucketScoresAfterCalculation(Map<SuspiciousEventScore, Double> baseEventScoreToCalculatedScoreMap) {
        for (Map.Entry<SuspiciousEventScore, Double> baseEventScoreToCalculatedScoreEntry : baseEventScoreToCalculatedScoreMap.entrySet()) {

            BehaviorEventsScoreType behaviorEventName = BehaviorEventsScoreType.enumFromString(baseEventScoreToCalculatedScoreEntry.getKey().getName());
            double score = baseEventScoreToCalculatedScoreEntry.getValue();

            // if the there is this behavior event today
            if (bucketBehaviorEventsData.getBehaviorEventTypeToCurrentEventCountAndContributionDataMap().containsKey(behaviorEventName)) {
                int numberOfBehaviorEvent = bucketBehaviorEventsData.getBehaviorEventTypeToCurrentEventCountAndContributionDataMap()
                        .get(behaviorEventName).getCount();
                bucketBehaviorEventsData.getBehaviorEventTypeToCurrentEventCountAndContributionDataMap()
                        .put(behaviorEventName, new EventCountAndContributionData(numberOfBehaviorEvent, score));
            }

            double tailContribution = 0;
            // if the there is this behavior event in the tail Events
            if (bucketBehaviorEventsData.getBehaviorEventTypeToOldEventsContributionMap().get(behaviorEventName) != null) {
                tailContribution = bucketBehaviorEventsData.getBehaviorEventTypeToOldEventsContributionMap().get(behaviorEventName).values().stream()
                        .mapToDouble(Number::doubleValue).sum();
            }

            bucketBehaviorEventsData.getBehaviorEventTypeToTotalEventsContributionMap()
                    .put(behaviorEventName, score + tailContribution);

        }
    }

    private String createBaseEventScoreFormula(BehaviorEventsScoreType baseEventScoreType) {
        SuspiciousEventScore suspiciousEventScore = behaviorEventsScore.getBaseEventScoreMap().get(baseEventScoreType);
        String contributionFunctionString = suspiciousEventScore.getContribution();

        if (contributionFunctionString != null && !contributionFunctionString.isEmpty()) {
            int eventsCount = 0;
            if (bucketBehaviorEventsData.getBehaviorEventTypeToCurrentEventCountAndContributionDataMap().get(baseEventScoreType) != null) {
                eventsCount = bucketBehaviorEventsData.getBehaviorEventTypeToCurrentEventCountAndContributionDataMap().get(baseEventScoreType).getCount();
            }
            contributionFunctionString = contributionFunctionString.replace("eventsNumber", Integer.toString(eventsCount));
        }

        return contributionFunctionString;
    }

    private void addTodayScoreToOldEventsMap() {

        Date today = DatesCalculation.setDateOnBeginningOfDay(new Date());
        for (Map.Entry<BehaviorEventsScoreType, EventCountAndContributionData> entry
                : bucketBehaviorEventsData.getBehaviorEventTypeToCurrentEventCountAndContributionDataMap().entrySet()) {


            BehaviorEventsScoreType behaviorEventType = entry.getKey();
            double score = entry.getValue().getContribution();

            Map<BehaviorEventsScoreType, Map<Date, Double>> behaviorEventTypeToOldEventsContributionMap = bucketBehaviorEventsData.getBehaviorEventTypeToOldEventsContributionMap();

            behaviorEventTypeToOldEventsContributionMap.putIfAbsent(behaviorEventType, new ConcurrentHashMap<>());
            behaviorEventTypeToOldEventsContributionMap.get(behaviorEventType).put(today, score);
        }
    }

    private boolean isEventsExistAfterDeletingVeryOldEvents(int daysDiff) {
        Map<BehaviorEventsScoreType, EventCountAndContributionData> behaviorEventTypeToCurrentEventsNumberAndContributionMap =
                bucketBehaviorEventsData.getBehaviorEventTypeToCurrentEventCountAndContributionDataMap();
        Map<BehaviorEventsScoreType, Map<Date, Double>> behaviorEventTypeToOldEventsContributionMap = bucketBehaviorEventsData.getBehaviorEventTypeToOldEventsContributionMap();

        List<SuspiciousEventScore> behaviorEventsScoreList = userTypeToBehaviorEventsScoreMap.get(bucketBehaviorEventsData.getUserType()).getSuspiciousEventScoreList();
        behaviorEventsScoreList.forEach(event -> {
            if (event.getTerm() > 0) {
                BehaviorEventsScoreType eventsScoreType = BehaviorEventsScoreType.enumFromString(event.getName());
                EventCountAndContributionData eventCountAndContributionData
                        = behaviorEventTypeToCurrentEventsNumberAndContributionMap.get(eventsScoreType);
                if (eventCountAndContributionData != null && daysDiff > event.getTerm()) {
                    behaviorEventTypeToCurrentEventsNumberAndContributionMap.remove(eventsScoreType);
                    behaviorEventTypeToOldEventsContributionMap.remove(eventsScoreType);
                    return;
                }

                Map<Date, Double> oldEventDateToContributionMap = behaviorEventTypeToOldEventsContributionMap.get(eventsScoreType);
                if (oldEventDateToContributionMap != null) {
                    oldEventDateToContributionMap =
                            oldEventDateToContributionMap.entrySet()
                                    .stream()
                                    .filter(p -> DatesCalculation.calculateDaysDiffBetweenDates(DatesCalculation.setDateOnBeginningOfDay(p.getKey()),
                                            DatesCalculation.setDateOnBeginningOfDay(new Date())) <= event.getTerm())
                                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                    behaviorEventTypeToOldEventsContributionMap.put(eventsScoreType, oldEventDateToContributionMap);
                }
            }
        });
        return !behaviorEventTypeToOldEventsContributionMap.isEmpty() || !behaviorEventTypeToCurrentEventsNumberAndContributionMap.isEmpty();
    }

    @Override
    protected void decayDailyEventScoresType(int daysDiff) {
        if (isEventsExistAfterDeletingVeryOldEvents(daysDiff)) {
            Map<BehaviorEventsScoreType, Map<Date, Double>> behaviorEventTypeToOldEventsContributionMap = bucketBehaviorEventsData.getBehaviorEventTypeToOldEventsContributionMap();

            addTodayScoreToOldEventsMap();

            Map<BehaviorEventsScoreType, Map<Date, Double>> behaviorEventTypeToOldEventsContributionMapAfterDecayed = new ConcurrentHashMap<>();

            // loop on every behaviorEvent
            for (Map.Entry<BehaviorEventsScoreType, Map<Date, Double>> behaviorEventTypeToOldEventsContributionEntry : behaviorEventTypeToOldEventsContributionMap.entrySet()) {
                SuspiciousEventScore suspiciousEventScore = getBaseEventScoreByEventScoreType(behaviorEventTypeToOldEventsContributionEntry.getKey());
                Map<Date, Double> dayToScoreMap = new ConcurrentHashMap<>();

                // loop on every date
                for (Map.Entry<Date, Double> dateToOldEventsContributionEntry : behaviorEventTypeToOldEventsContributionEntry.getValue().entrySet()) {
                    EventDecay behaviorEventDecay = new EventDecay(suspiciousEventScore, dateToOldEventsContributionEntry.getValue());
                    Pair<SuspiciousEventScore, Double> decayedScores = new DecayCalculator<SuspiciousEventScore>().calculateEntry(behaviorEventDecay, daysDiff);
                    dayToScoreMap.put(dateToOldEventsContributionEntry.getKey(), decayedScores.getValue());
                }
                behaviorEventTypeToOldEventsContributionMapAfterDecayed.put(BehaviorEventsScoreType.enumFromString(suspiciousEventScore.getName()), dayToScoreMap);
            }
            bucketBehaviorEventsData.setBehaviorEventTypeToOldEventsContributionMap(behaviorEventTypeToOldEventsContributionMapAfterDecayed);
        }
        bucketBehaviorEventsData.getBehaviorEventTypeToCurrentEventCountAndContributionDataMap().clear();
    }

    private double getWeightByEventScore(BehaviorEventsScoreType eventScoreType) {
        if (behaviorEventsScore.getBaseEventScoreMap().get(eventScoreType) == null) {
            return 0;
        }
        return behaviorEventsScore.getBaseEventScoreMap().get(eventScoreType).getWeight();
    }

    private SuspiciousEventScore getBaseEventScoreByEventScoreType(BehaviorEventsScoreType behaviorEventScore) {
        return behaviorEventsScore.getBaseEventScoreMap().get(behaviorEventScore);
    }

    public double getBucketSumScore(BucketBehaviorEventsData bucketBehaviorEventsData) {
        double sumScore = 0;
        for (Map.Entry<BehaviorEventsScoreType, Double> eventScoresToFunctionalScoreMapEntry : bucketBehaviorEventsData.getBehaviorEventTypeToTotalEventsContributionMap().entrySet()) {
            sumScore += eventScoresToFunctionalScoreMapEntry.getValue() * getWeightByEventScore(eventScoresToFunctionalScoreMapEntry.getKey());
        }
        return sumScore;
    }
}
