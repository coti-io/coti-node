package io.coti.trustscore.services.calculationServices;

import io.coti.trustscore.config.rules.BaseEventScore;
import io.coti.trustscore.config.rules.BehaviorEventsScore;
import io.coti.trustscore.config.rules.RulesData;
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
                collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue().getBehaviorEventsScore()));
    }

    @Override
    public void setCurrentScores() {
        Map<BaseEventScore, String> baseEventScoreToCalculationFormulaMap = new ConcurrentHashMap<>();

        for (Map.Entry<BehaviorEventsScoreType, BaseEventScore> baseEventTypeToBaseEventScoreEntry : behaviorEventsScore.getBaseEventScoreMap().entrySet()) {

            BaseEventScore baseEventScore = baseEventTypeToBaseEventScoreEntry.getValue();
            String scoreFormulaCalculation = createBaseEventScoreFormula(baseEventTypeToBaseEventScoreEntry.getKey());

            baseEventScoreToCalculationFormulaMap.put(baseEventScore, scoreFormulaCalculation);
        }

        ScoreCalculator scoreCalculator = new ScoreCalculator(baseEventScoreToCalculationFormulaMap);
        Map<BaseEventScore, Double> baseEventScoreToCalculatedScoreMap = scoreCalculator.calculate();
        updateBucketScoresAfterCalculation(baseEventScoreToCalculatedScoreMap);

    }

    private void updateBucketScoresAfterCalculation(Map<BaseEventScore, Double> baseEventScoreToCalculatedScoreMap) {
        for (Map.Entry<BaseEventScore, Double> baseEventScoreToCalculatedScoreEntry : baseEventScoreToCalculatedScoreMap.entrySet()) {

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
            // if the there is this behavior event in the tail events
            if (bucketBehaviorEventsData.getBehaviorEventTypeToOldEventsContributionMap().get(behaviorEventName) != null) {
                tailContribution = bucketBehaviorEventsData.getBehaviorEventTypeToOldEventsContributionMap().get(behaviorEventName).values().stream()
                        .mapToDouble(Number::doubleValue).sum();
            }

            bucketBehaviorEventsData.getBehaviorEventTypeToTotalEventsContributionMap()
                    .put(behaviorEventName, score + tailContribution);

        }
    }

    private String createBaseEventScoreFormula(BehaviorEventsScoreType baseEventScoreType) {
        BaseEventScore baseEventScore = behaviorEventsScore.getBaseEventScoreMap().get(baseEventScoreType);
        String contributionFunctionString = baseEventScore.getContribution();

        int eventsCount = 0;
        if (bucketBehaviorEventsData.getBehaviorEventTypeToCurrentEventCountAndContributionDataMap().get(baseEventScoreType) != null) {
            eventsCount = bucketBehaviorEventsData.getBehaviorEventTypeToCurrentEventCountAndContributionDataMap().get(baseEventScoreType).getCount();
        }
        contributionFunctionString = contributionFunctionString.replace("eventsNumber", Integer.toString(eventsCount));

        return contributionFunctionString;
    }

    private void addTodayScoreToOldEventsMap() {

        Date today = DatesCalculation.setDateOnBeginningOfDay(new Date());
        for (Map.Entry<BehaviorEventsScoreType, EventCountAndContributionData> entry
                : bucketBehaviorEventsData.getBehaviorEventTypeToCurrentEventCountAndContributionDataMap().entrySet()) {


            BehaviorEventsScoreType BehaviorEventType = entry.getKey();
            double score = entry.getValue().getContribution();

            Map<BehaviorEventsScoreType, Map<Date, Double>> behaviorEventTypeToOldEventsContributionMap = bucketBehaviorEventsData.getBehaviorEventTypeToOldEventsContributionMap();
            if (behaviorEventTypeToOldEventsContributionMap.get(BehaviorEventType) == null) {
                behaviorEventTypeToOldEventsContributionMap.put(BehaviorEventType, new ConcurrentHashMap<>());
            }
            behaviorEventTypeToOldEventsContributionMap.get(BehaviorEventType).put(today, score);
        }
    }

    private boolean isEventsExistAfterDeletingVeryOldEvents(int daysDiff) {
        Map<BehaviorEventsScoreType, EventCountAndContributionData> behaviorEventTypeToCurrentEventsNumberAndContributionMap =
                bucketBehaviorEventsData.getBehaviorEventTypeToCurrentEventCountAndContributionDataMap();
        Map<BehaviorEventsScoreType, Map<Date, Double>> behaviorEventTypeToOldEventsContributionMap = bucketBehaviorEventsData.getBehaviorEventTypeToOldEventsContributionMap();

        List<BaseEventScore> behaviorEventsScoreList = userTypeToBehaviorEventsScoreMap.get(bucketBehaviorEventsData.getUserType()).getBaseEventScoreList();
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
                                    .collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));
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
                BaseEventScore baseEventScore = getBaseEventScoreByEventScoreType(behaviorEventTypeToOldEventsContributionEntry.getKey());
                Map<Date, Double> dayToScoreMap = new ConcurrentHashMap<>();

                // loop on every date
                for (Map.Entry<Date, Double> dateToOldEventsContributionEntry : behaviorEventTypeToOldEventsContributionEntry.getValue().entrySet()) {
                    EventDecay behaviorEventDecay = new EventDecay(baseEventScore, dateToOldEventsContributionEntry.getValue());
                    Pair<BaseEventScore, Double> decayedScores = new DecayCalculator().calculateEntry(behaviorEventDecay, daysDiff);
                    dayToScoreMap.put(dateToOldEventsContributionEntry.getKey(), decayedScores.getValue());
                }
                behaviorEventTypeToOldEventsContributionMapAfterDecayed.put(BehaviorEventsScoreType.enumFromString(baseEventScore.getName()), dayToScoreMap);
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

    private BaseEventScore getBaseEventScoreByEventScoreType(BehaviorEventsScoreType behaviorEventScore) {
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
